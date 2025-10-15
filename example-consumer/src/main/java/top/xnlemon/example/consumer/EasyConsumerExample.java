package top.xnlemon.example.consumer;

import top.xnlemon.example.common.model.User;
import top.xnlemon.example.common.service.UserService;
import top.xnlemon.nenerpc.proxy.ServiceProxyFactory;


public class EasyConsumerExample {
    public static void main(String[] args) {
        // todo 需要获取 UserService 的实现类对象
        //UserService userService = new UserServiceProxy();
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
