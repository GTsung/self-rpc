package com.gt.rpc.client.route.impl;

import com.gt.rpc.client.route.RpcLoadBalance;
import com.gt.rpc.protocol.RpcProtocol;

import java.util.List;
import java.util.Random;

/**
 * @author GTsung
 * @date 2022/1/21
 */
public class RpcLoadBalanceRandom extends RpcLoadBalance {

    private Random random = new Random();

    @Override
    protected RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        int size = addressList.size();
        return addressList.get(random.nextInt(size));
    }
}
