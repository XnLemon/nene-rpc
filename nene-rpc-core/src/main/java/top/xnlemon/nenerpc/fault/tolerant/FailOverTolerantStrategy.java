package top.xnlemon.nenerpc.fault.tolerant;

import cn.hutool.core.collection.CollUtil;
import top.xnlemon.nenerpc.RpcApplication;
import top.xnlemon.nenerpc.fault.retry.RetryStrategy;
import top.xnlemon.nenerpc.fault.retry.RetryStrategyFactory;
import top.xnlemon.nenerpc.loadbalancer.LoadBalancer;
import top.xnlemon.nenerpc.loadbalancer.LoadBalancerFactory;
import top.xnlemon.nenerpc.model.RpcRequest;
import top.xnlemon.nenerpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import top.xnlemon.nenerpc.model.ServiceMetaInfo;
import top.xnlemon.nenerpc.config.RpcConfig;
import top.xnlemon.nenerpc.registry.Registry;
import top.xnlemon.nenerpc.registry.RegistryFactory;
import top.xnlemon.nenerpc.server.tcp.VertxTcpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转移到其他服务节点 - 容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {


        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        //获取其它节点并调用
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("serviceMetaInfoList");
        ServiceMetaInfo selectedServiceMetaInfo = (ServiceMetaInfo) context.get("selectedServiceMetaInfo");
        //检查一下上下文调用
        if (rpcRequest == null || serviceMetaInfoList == null || selectedServiceMetaInfo == null) {
            throw new IllegalArgumentException("FailOver 上下文缺失必要参数，请检查调用方传入的 context");
        }


        log.warn("触发 FailOver 容错策略，当前节点：{}，异常：{}",
                selectedServiceMetaInfo, e.toString());


        // 注册中心中删除故障节点
        try {
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            registry.unRegister(selectedServiceMetaInfo);
            log.warn("FailOver: 已在注册中心下线故障节点：{}", selectedServiceMetaInfo);
        } catch (Exception registryEx) {
            log.error("FailOver: 下线故障节点到注册中心失败，node={} ex={}",
                    selectedServiceMetaInfo, registryEx.toString());
        }

        // 创建可修改的节点列表
        List<ServiceMetaInfo> availableNodes = new ArrayList<>(serviceMetaInfoList);
        availableNodes.remove(selectedServiceMetaInfo);
        if (CollUtil.isEmpty(availableNodes)) {
            throw new RuntimeException("容错策略后暂无服务地址！", e);
        }



        // 重新选择一个节点
        Map<String,Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        ServiceMetaInfo nextSelectedServiceMetaInfo = loadBalancer.select(requestParams,availableNodes);
        log.info("FailOver 选择新节点：{}", nextSelectedServiceMetaInfo);

        RpcResponse rpcResponse;
        try {
            // 发送 TCP 请求
            // 使用重试机制
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, nextSelectedServiceMetaInfo)
            );
            return rpcResponse;
        } catch (Exception ex) {
            // 建议这里不要再用全局容错策略 = failOver，避免递归
            // 可以指定一个专门的降级策略
            TolerantStrategy tolerantStrategy = new FailBackTolerantStrategy();
            log.warn("触发 FailOver 容错策略后重试节点仍失败！当前节点：{}，异常：{}",
                    nextSelectedServiceMetaInfo, ex.toString());

            Map<String, Object> requestTolerantParamMap = new HashMap<>();
            requestTolerantParamMap.put("rpcRequest",rpcRequest);
            requestTolerantParamMap.put("selectedServiceMetaInfo",nextSelectedServiceMetaInfo);
            requestTolerantParamMap.put("serviceMetaInfoList",availableNodes);
            rpcResponse = tolerantStrategy.doTolerant(requestTolerantParamMap, ex);
        }
        return rpcResponse;
    }
}
