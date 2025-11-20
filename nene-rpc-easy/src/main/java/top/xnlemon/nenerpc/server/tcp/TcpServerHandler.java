package top.xnlemon.nenerpc.server.tcp;

import top.xnlemon.nenerpc.model.RpcRequest;
import top.xnlemon.nenerpc.model.RpcResponse;
import top.xnlemon.nenerpc.protocol.ProtocolMessage;
import top.xnlemon.nenerpc.protocol.ProtocolMessageDecoder;
import top.xnlemon.nenerpc.protocol.ProtocolMessageEncoder;
import top.xnlemon.nenerpc.protocol.ProtocolMessageTypeEnum;
import top.xnlemon.nenerpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket socket) {

        TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                // 接受请求，解码
                ProtocolMessage<RpcRequest> protocolMessage;
                try {
                    protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
                } catch (IOException e) {
                    throw new RuntimeException("协议消息解码错误");
                }
                RpcRequest rpcRequest = protocolMessage.getBody();

                // 处理请求
                // 构造响应结果对象
                RpcResponse rpcResponse = new RpcResponse();
                try {
                    // 获取要调用的服务实现类，通过反射调用
                    Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                    Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                    Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                    // 封装返回结果
                    rpcResponse.setData(result);
                    rpcResponse.setDataType(method.getReturnType());
                    rpcResponse.setMessage("ok");
                } catch (Exception e) {
                    e.printStackTrace();
                    rpcResponse.setMessage(e.getMessage());
                    rpcResponse.setException(e);
                }

                // 发送响应，编码
                ProtocolMessage.Header header = protocolMessage.getHeader();
                header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
                ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
                try {
                    Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                    socket.write(encode);
                } catch (IOException e) {
                    throw new RuntimeException("协议消息编码错误");
                }
            });
            socket.handler(tcpBufferHandlerWrapper);
        }
    }

