package top.xnlemon.example.provider;


import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.registry.LocalRegistry;
import top.xnlemon.nenerpc.server.HttpServer;
import top.xnlemon.nenerpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 提供服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
