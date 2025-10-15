package top.xnlemon.nenerpc.registry;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
  先不用第三方 测试 本地注册器
 */
public class LocalRegistry {
    /**
     * 注册信息存储
     */
    private static final Map<String, Class<?>> registryMap = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass) {
        registryMap.put(serviceName, implClass);
    }

    /**
     * 获取服务
     * @Param serviceName
     * @return
     */
    public static Class<?> get(String serviceName) {
        return registryMap.get(serviceName);
    }

    /**
     * 删除服务
     * @Param serviceName
     */
    public static void remove(String serviceName) {
        registryMap.remove(serviceName);
    }
}
