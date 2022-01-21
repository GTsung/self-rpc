package com.gt.rpc.client.route.impl;

import com.gt.rpc.client.route.RpcLoadBalance;
import com.gt.rpc.protocol.RpcProtocol;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author GTsung
 * @date 2022/1/20
 */
public class RpcLoadBalanceRoundRobin extends RpcLoadBalance {

    private AtomicInteger roundRobin = new AtomicInteger(0);

    @Override
    protected RpcProtocol doRoute(String serviceKey, List<RpcProtocol> result) {
        int size = result.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return result.get(index);
    }
}
