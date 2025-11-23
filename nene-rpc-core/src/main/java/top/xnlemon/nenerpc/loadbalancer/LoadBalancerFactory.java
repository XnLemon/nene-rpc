package top.xnlemon.nenerpc.loadbalancer;


import top.xnlemon.nenerpc.spi.SpiLoader;

public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    /*** 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    /**
     * 获取示例
     */

    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
