package top.xnlemon.example.consumer;

import top.xnlemon.example.common.model.User;
import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.proxy.ServiceProxyFactory;

/**
 * 服务消费者示例
 *
 */
public class ConsumerExample {

    public static void main(String[] args) {


        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("neneko");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
