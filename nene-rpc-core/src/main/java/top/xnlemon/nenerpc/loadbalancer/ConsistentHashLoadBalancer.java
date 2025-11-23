package top.xnlemon.nenerpc.loadbalancer;

import top.xnlemon.nenerpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer implements LoadBalancer{

    /**
     * Hash环
     */
    private final TreeMap <Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * 虚拟节点数
     */
    private static final int VIRTUAL_NODE_NUM = 100;

    /**
     * 一致性哈希
     * @param requestParams       请求参数
     * @param serviceMetaInfoList 可用服务列表
     * @return
     */
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList.isEmpty()){
            return null;
        }

        //建环
        for(ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList){
            for(int i = 0; i < VIRTUAL_NODE_NUM; i++){
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }

        int hash = getHash(requestParams);

        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);

        if(entry == null){
            entry = virtualNodes.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * Hash算法
     */
    private int getHash(Object key){
        return key.hashCode();
    }
}
