package top.xnlemon.example.provider;

import  top.xnlemon.example.common.model.User;
import  top.xnlemon.example.common.service.UserService;

public class UserServiceImpl implements UserService {

    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        return user;
    }
}
