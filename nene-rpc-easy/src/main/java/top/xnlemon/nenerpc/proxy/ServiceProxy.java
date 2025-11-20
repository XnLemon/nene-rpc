package top.xnlemon.nenerpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import lombok.extern.slf4j.Slf4j;
import top.xnlemon.nenerpc.RpcApplication;
import top.xnlemon.nenerpc.config.RpcConfig;
import top.xnlemon.nenerpc.constant.RpcConstant;
import top.xnlemon.nenerpc.model.RpcRequest;
import top.xnlemon.nenerpc.model.RpcResponse;
import top.xnlemon.nenerpc.model.ServiceMetaInfo;
import top.xnlemon.nenerpc.protocol.*;
import top.xnlemon.nenerpc.registry.Registry;
import top.xnlemon.nenerpc.registry.RegistryFactory;
import top.xnlemon.nenerpc.serializer.SerializerFactory;
import top.xnlemon.nenerpc.serializer.Serializer;
import top.xnlemon.nenerpc.server.tcp.VertxTcpClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
		try {
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
			ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
			// 发送 TCP 请求
			RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
			return rpcResponse.getData();
		} catch (Exception e) {
			throw new RuntimeException("调用失败");
		}
	}
}

