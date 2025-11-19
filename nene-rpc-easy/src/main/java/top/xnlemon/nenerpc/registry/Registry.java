package top.xnlemon.nenerpc.registry;

import top.xnlemon.nenerpc.config.RegistryConfig;
import top.xnlemon.nenerpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心接口
 * 定义了服务注册与发现的核心功能
 */
public interface Registry {

    /**
     * 初始化注册中心
     *
     * @param registryConfig 注册中心配置信息
     *                      包含注册中心地址、端口等参数
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务（服务端）
     * 将服务信息注册到注册中心，供消费端发现

     *
     * @param serviceMetaInfo 服务元信息
     *                       包含服务名称、版本、地址、端口等
     * @throws Exception 注册过程中可能出现的异常
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注销服务（服务端）
     * 从注册中心移除已注册的服务

     *
     * @param serviceMetaInfo 服务元信息
     *                       指明要注销的服务信息
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     * 服务发现（获取某服务的所有节点，消费端）
     * 从注册中心获取指定服务的所有可用节点
     *
     * @param serviceKey 服务键名

     *                   通常由服务名称和版本组成
     * @return 服务节点列表，包含所有可用的服务实例信息
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 心跳检测（服务端）
     * 定期向注册中心发送心跳，保持服务节点的活跃状态
     */
    void heartBeat();

    /**
     * 监听（消费端）
     * 监听指定服务节点的变化，实现动态感知服务状态

     *
     * @param serviceNodeKey 服务节点键
     *                      用于标识要监听的具体服务节点
     */
    void watch(String serviceNodeKey);

    /**
     * 服务销毁
     * 清理注册中心资源，关闭连接等
     * 在系统关闭或服务下线时调用
     */
    void destroy();
}
