package top.xnlemon.nenerpc.server;


import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import top.xnlemon.nenerpc.model.RpcRequest;
import top.xnlemon.nenerpc.model.RpcResponse;
import top.xnlemon.nenerpc.registry.LocalRegistry;
import top.xnlemon.nenerpc.serializer.Serializer;
import top.xnlemon.nenerpc.RpcApplication;
import top.xnlemon.nenerpc.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.Method;


public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        //序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        //记录日志
        System.out.println("httpServerRequest = " + request.method() + " " + request.uri());

        //异步处理HTTP请求
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //构造结果对象
            RpcResponse rpcResponse = new RpcResponse();
            //if null , return
            if (rpcRequest == null) {
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(request, rpcResponse, serializer);
                return;
            }


            try {
                //执行方法
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method  = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                //返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("OK");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            doResponse(request, rpcResponse, serializer);
        });
    }

    /**
     * 响应
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse response = request.response()
                .putHeader("Content-Type", "application/json");
        try {
            //序列化
            byte[] serialized = serializer.serialize(rpcResponse);
            response.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            response.end(Buffer.buffer());
        }
    }


}