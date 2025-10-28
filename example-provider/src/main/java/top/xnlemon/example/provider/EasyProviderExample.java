package top.xnlemon.example.provider;


import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.registry.LocalRegistry;
import top.xnlemon.nenerpc.registry.Registry;
import top.xnlemon.nenerpc.registry.ZooKeeperRegistry;
import top.xnlemon.nenerpc.server.HttpServer;
import top.xnlemon.nenerpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务实现到本地映射（服务端反射用）
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

//        // 向注册中心注册对外可访问地址
//        Registry registry = new ZooKeeperRegistry("localhost:2181");
//        String serviceName = UserService.class.getName();
//        String address = "http://localhost:8080";
//        registry.register(serviceName, address);

        // 提供服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
