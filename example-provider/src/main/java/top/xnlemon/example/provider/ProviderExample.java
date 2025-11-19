package top.xnlemon.example.provider;

import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.RpcApplication;
import top.xnlemon.nenerpc.config.RegistryConfig;
import top.xnlemon.nenerpc.config.RpcConfig;
import top.xnlemon.nenerpc.model.ServiceMetaInfo;
import top.xnlemon.nenerpc.registry.LocalRegistry;
import top.xnlemon.nenerpc.registry.Registry;
import top.xnlemon.nenerpc.registry.RegistryFactory;
import top.xnlemon.nenerpc.server.HttpServer;
import top.xnlemon.nenerpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class ProviderExample {
    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
