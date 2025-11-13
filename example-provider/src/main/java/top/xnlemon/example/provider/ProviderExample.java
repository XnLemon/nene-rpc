package top.xnlemon.example.provider;

import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.RpcApplication;
import top.xnlemon.nenerpc.registry.LocalRegistry;
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
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
