package top.xnlemon.example.consumer;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import top.xnlemon.example.common.model.User;
import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.model.RpcRequest;
import top.xnlemon.nenerpc.model.RpcResponse;
import top.xnlemon.nenerpc.serializer.JdkSerializer;
import top.xnlemon.nenerpc.serializer.Serializer;

import java.io.IOException;

public class UserServiceProxy implements UserService {

    public User getUser(User user) {
        //指定序列化器
        Serializer serializer = new JdkSerializer();

        //调用远程服务
        RpcRequest request = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try{
            byte[] bodybytes = serializer.serialize(request);
            byte[] result;
            try(HttpResponse response = HttpRequest.post("http://localhost:8080")
                    .body(bodybytes)
                    .execute()) {
                result = response.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
