package top.xnlemon.example.consumer;

import top.xnlemon.example.common.model.User;
import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.config.RpcConfig;
import top.xnlemon.nenerpc.proxy.ServiceProxyFactory;
import top.xnlemon.nenerpc.utils.ConfigUtils;


/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        //UserService userService = new UserServiceProxy();
//        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
//        User user = new User();
//        user.setName("neneko");
//        // 调用
//        User newUser = userService.getUser(user);
//        if (newUser != null) {
//            System.out.println(newUser.getName());
//        } else {
//            System.out.println("user == null");
//        }
            RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
            System.out.println(rpc);
        }
    }


