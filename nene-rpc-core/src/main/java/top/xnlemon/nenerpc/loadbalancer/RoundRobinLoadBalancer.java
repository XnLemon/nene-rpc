package top.xnlemon.nenerpc.loadbalancer;

import top.xnlemon.nenerpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer{

    /**
     * 轮询下标
     */
     private final AtomicInteger currentIndex = new AtomicInteger(0);


     /**
     * @param requestParams       请求参数
     * @param serviceMetaInfoList 可用服务列表
     * @return
     */
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
       if(serviceMetaInfoList.isEmpty()) {
           return null;
       }
       //1服务
        if(serviceMetaInfoList.size() == 1) {
            return serviceMetaInfoList.get(0);
        }
        int index = currentIndex.getAndIncrement() % serviceMetaInfoList.size();
        return serviceMetaInfoList.get(index);
    }
}
