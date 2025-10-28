package top.xnlemon.example.provider;


import lombok.extern.slf4j.Slf4j;
import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.config.RegistryConfig;
import top.xnlemon.nenerpc.config.RpcConfig;
import top.xnlemon.nenerpc.model.ServiceMetaInfo;
import top.xnlemon.nenerpc.registry.LocalRegistry;
import top.xnlemon.nenerpc.registry.Registry;
import top.xnlemon.nenerpc.registry.ZooKeeperRegistry;
import top.xnlemon.nenerpc.server.HttpServer;
import top.xnlemon.nenerpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
@Slf4j
public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务实现到本地映射（服务端反射用）
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 基于 ServiceMetaInfo 向注册中心（ZK）注册
        RpcConfig rpcConfig = new RpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = new ZooKeeperRegistry();
        registry.init(registryConfig);


        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(UserService.class.getName());
        serviceMetaInfo.setServiceVersion(rpcConfig.getVersion());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
            log.info("服务注册成功:{}", serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException("服务注册失败", e);
        }

        // 下线
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));

        // 提供服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8081);
    }
}
