package com.gt.rpc.client.route;

import com.gt.rpc.client.handler.RpcClientHandler;
import com.gt.rpc.protocol.RpcProtocol;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author GTsung
 * @date 2022/1/20
 */
public class RpcLoadBalanceRoundRobin extends RpcLoadBalance {

    private AtomicInteger roundRobin = new AtomicInteger(0);


    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> result = serviceMap.get(serviceKey);
        if (!CollectionUtils.isEmpty(result)) {
            return doRoute(result);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }

    private RpcProtocol doRoute(List<RpcProtocol> result) {
        int size = result.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return result.get(index);
    }
}
