package com.gt.rpc.client.route;

import com.gt.rpc.client.handler.RpcClientHandler;
import com.gt.rpc.protocol.RpcProtocol;
import com.gt.rpc.util.ServiceUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author GTsung
 * @date 2022/1/20
 */
public abstract class RpcLoadBalance {

    private Map<String, List<RpcProtocol>> getServiceMap(Map<RpcProtocol, RpcClientHandler> connectedServerNodes) {
        Map<String, List<RpcProtocol>> serviceMap = new HashedMap<>();
        if (connectedServerNodes != null && connectedServerNodes.size() > 0) {
            connectedServerNodes.keySet().forEach(key -> {
                key.getServiceInfoList().forEach(rpcServiceInfo -> {
                    String serviceKey = ServiceUtil.makeServiceKey(rpcServiceInfo.getServiceName(), rpcServiceInfo.getVersion());
                    List<RpcProtocol> rpcProtocols = serviceMap.get(serviceKey);
                    if (rpcProtocols == null) {
                        rpcProtocols = new ArrayList<>();
                    }
                    rpcProtocols.add(key);
                    serviceMap.putIfAbsent(serviceKey, rpcProtocols);
                });
            });
        }
        return serviceMap;
    }

    /**
     * 負載
     * @param serviceKey
     * @param connectedServerNodes
     * @return
     * @throws Exception
     */
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes)
            throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (!CollectionUtils.isEmpty(addressList)) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }

    protected abstract RpcProtocol doRoute(String serviceKey, List<RpcProtocol> protocols);

}
