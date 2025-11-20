package top.xnlemon.nenerpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        //创建VertX实例
        Vertx vertx = Vertx.vertx();

        //创建Http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        //监听端口 处理请求
        server.requestHandler(new HttpServerHandler());

        //启动服务器 监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server started on port " + port);
            }else {
                System.err.println("Failed to start server: " + result.cause());
            }
        });
    }
}
