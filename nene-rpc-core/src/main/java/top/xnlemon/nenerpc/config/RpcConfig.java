package top.xnlemon.nenerpc.config;

import top.xnlemon.nenerpc.fault.retry.RetryStrategyKeys;
import top.xnlemon.nenerpc.fault.tolerant.TolerantStrategyKeys;
import top.xnlemon.nenerpc.loadbalancer.LoadBalancerKeys;
import top.xnlemon.nenerpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架全局配置
 *

 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "nene-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8081;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.FIXED_INTERVAL;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_BACK;

    /**
     * 模拟调用
     */
    private boolean mock = true;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
