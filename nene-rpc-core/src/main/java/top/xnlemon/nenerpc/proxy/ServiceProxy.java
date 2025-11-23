package top.xnlemon.nenerpc.proxy;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import top.xnlemon.nenerpc.RpcApplication;
import top.xnlemon.nenerpc.config.RpcConfig;
import top.xnlemon.nenerpc.constant.RpcConstant;
import top.xnlemon.nenerpc.fault.retry.RetryStrategy;
import top.xnlemon.nenerpc.fault.retry.RetryStrategyFactory;
import top.xnlemon.nenerpc.fault.tolerant.TolerantStrategy;
import top.xnlemon.nenerpc.fault.tolerant.TolerantStrategyFactory;
import top.xnlemon.nenerpc.loadbalancer.LoadBalancer;
import top.xnlemon.nenerpc.loadbalancer.LoadBalancerFactory;
import top.xnlemon.nenerpc.model.RpcRequest;
import top.xnlemon.nenerpc.model.RpcResponse;
import top.xnlemon.nenerpc.model.ServiceMetaInfo;
import top.xnlemon.nenerpc.registry.Registry;
import top.xnlemon.nenerpc.registry.RegistryFactory;
import top.xnlemon.nenerpc.serializer.SerializerFactory;
import top.xnlemon.nenerpc.serializer.Serializer;
import top.xnlemon.nenerpc.server.tcp.VertxTcpClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Slf4j
public class ServiceProxy implements InvocationHandler {

	/**
	 * 调用代理
	 *
	 * @return
	 * @throws Throwable
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 指定序列化器
		final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

		// 构造请求
		String serviceName = method.getDeclaringClass().getName();
		RpcRequest rpcRequest = RpcRequest.builder()
				.serviceName(serviceName)
				.methodName(method.getName())
				.parameterTypes(method.getParameterTypes())
				.args(args)
				.build();
		// 序列化
		byte[] bodyBytes = serializer.serialize(rpcRequest);
		// 从注册中心获取服务提供者请求地址
		RpcConfig rpcConfig = RpcApplication.getRpcConfig();
		Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
		ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
		serviceMetaInfo.setServiceName(serviceName);
		serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
		List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
		if (CollUtil.isEmpty(serviceMetaInfoList)) {
			throw new RuntimeException("暂无服务地址");
		}
		LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
		Map<String,Object> requestParams = new HashMap<>();
		requestParams.put("methodName", rpcRequest.getMethodName());
		ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams,serviceMetaInfoList);


		RpcResponse rpcResponse;
		try {

			// 发送 TCP 请求
			// 使用重试机制
			RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
			rpcResponse = retryStrategy.doRetry(() ->
					VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
			);
			return rpcResponse.getData();
		} catch (Exception e) {
			// 容错机制
			TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
			Map<String, Object> requestTolerantParamMap = new HashMap<>();
			requestTolerantParamMap.put("rpcRequest",rpcRequest);
			requestTolerantParamMap.put("selectedServiceMetaInfo",selectedServiceMetaInfo);
			requestTolerantParamMap.put("serviceMetaInfoList",serviceMetaInfoList);
			rpcResponse = tolerantStrategy.doTolerant(requestTolerantParamMap, e);
		}
		return rpcResponse.getData();
	}
}

