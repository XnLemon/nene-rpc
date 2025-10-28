package top.xnlemon.nenerpc.config;

import top.xnlemon.nenerpc.registry.RegistryKeys;
import lombok.Data;


/**
 * RPC 框架注册中心配置类
 *
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class RegistryConfig {

    /**
     * 注册中心类别
     * 支持多种注册中心实现
     */
    private String registry = RegistryKeys.ZOOKEEPER;

    /**
     * 注册中心地址
     * 默认值为本地服务器的地址和端口
     */
    private String address = RegistryKeys.ZOOKEEPER_ADDRESS;

    /**
     * 用户名
     * 用于认证的用户名，可为空
     */
    private String username;

    /**
     * 密码
     * 用于认证的密码，可为空
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     * 默认值为 10000 毫秒（10秒）
     */
    private Long timeout = 10000L;
}
