package com.gt.rpc.client.route.impl;

import com.google.common.hash.Hashing;
import com.gt.rpc.client.route.RpcLoadBalance;
import com.gt.rpc.protocol.RpcProtocol;

import java.util.List;

/**
 * @author GTsung
 * @date 2022/1/21
 */
public class RpcLoadBalanceConsistentHash extends RpcLoadBalance {

    @Override
    protected RpcProtocol doRoute(String serviceKey, List<RpcProtocol> protocols) {
        int index = Hashing.consistentHash(serviceKey.hashCode(), protocols.size());
        return protocols.get(index);
    }
}
