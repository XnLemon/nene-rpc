package top.xnlemon.nenerpc.proxy;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import top.xnlemon.nenerpc.config.RegistryConfig;
import top.xnlemon.nenerpc.config.RpcConfig;
import top.xnlemon.nenerpc.model.RpcRequest;
import top.xnlemon.nenerpc.model.RpcResponse;
import top.xnlemon.nenerpc.model.ServiceMetaInfo;
import top.xnlemon.nenerpc.registry.Registry;
import top.xnlemon.nenerpc.registry.ZooKeeperRegistry;
import top.xnlemon.nenerpc.serializer.JdkSerializer;
import top.xnlemon.nenerpc.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class ServiceProxy implements InvocationHandler{

	/**
	 * 调用代理
	 * @return
	 * @throws Throwable
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 序列化器
		final Serializer serializer = new JdkSerializer();

		// 1. 组装 RPC 请求
		String serviceName = method.getDeclaringClass().getName();
		RpcRequest rpcRequest = RpcRequest.builder()
				.serviceName(serviceName)
				.methodName(method.getName())
				.parameterTypes(method.getParameterTypes())
				.args(args)
				.build();

		// 2. 基于 ZK 注册中心做服务发现
		RpcConfig rpcConfig = new RpcConfig();
		RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
		Registry registry = new ZooKeeperRegistry();
		registry.init(registryConfig);

		ServiceMetaInfo keyMeta = new ServiceMetaInfo();
		keyMeta.setServiceName(serviceName);
		keyMeta.setServiceVersion(rpcConfig.getVersion());
		String serviceKey = keyMeta.getServiceKey();

		List<ServiceMetaInfo> instances = registry.serviceDiscovery(serviceKey);
		if (instances == null || instances.isEmpty()) {
			throw new RuntimeException("未发现可用服务实例: " + serviceKey);
		}
		log.info("可用服务实例: {}", instances);

		// 简单选择第一个实例（可替换为负载均衡）
		ServiceMetaInfo target = instances.get(0);
		String url = target.getServiceAddress();

		// 3. 发起 HTTP 调用
		byte[] body = serializer.serialize(rpcRequest);
		byte[] resultBytes;
		try (HttpResponse response = HttpRequest.post(url)
				.body(body)
				.execute()) {
			resultBytes = response.bodyBytes();
		}

		// 4. 反序列化响应并返回
		RpcResponse rpcResponse = serializer.deserialize(resultBytes, RpcResponse.class);
		if (rpcResponse == null) {
			throw new RuntimeException("RPC 响应为空");
		}
		if (rpcResponse.getException() != null) {
			throw rpcResponse.getException();
		}
		return rpcResponse.getData();
	}
}
