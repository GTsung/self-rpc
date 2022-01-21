package com.gt.rpc.client.route.impl;

import com.gt.rpc.client.route.RpcLoadBalance;
import com.gt.rpc.protocol.RpcProtocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author GTsung
 * @date 2022/1/21
 */
public class RpcLoadBalanceLRU extends RpcLoadBalance {

    private ConcurrentMap<String, LinkedHashMap<RpcProtocol, RpcProtocol>> jobLRUMap = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    @Override
    protected RpcProtocol doRoute(String serviceKey, List<RpcProtocol> protocols) {
        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        // init lru
        LinkedHashMap<RpcProtocol, RpcProtocol> lruHashMap = jobLRUMap.get(serviceKey);
        if (lruHashMap == null) {
            /**
             * LinkedHashMap
             * a、accessOrder：ture=访问顺序排序（get/put时排序）/ACCESS-LAST；false=插入顺序排期/FIFO；
             * b、removeEldestEntry：新增元素时将会调用，返回true时会删除最老元素；
             *      可封装LinkedHashMap并重写该方法，比如定义最大容量，超出是返回true即可实现固定长度的LRU算法；
             */
            lruHashMap = new LinkedHashMap<RpcProtocol, RpcProtocol>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<RpcProtocol, RpcProtocol> eldest) {
                    if (super.size() > 1000) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            jobLRUMap.putIfAbsent(serviceKey, lruHashMap);
        }

        // put new
        for (RpcProtocol address : protocols) {
            if (!lruHashMap.containsKey(address)) {
                lruHashMap.put(address, address);
            }
        }
        // remove old
        List<RpcProtocol> delKeys = new ArrayList<>();
        for (RpcProtocol existKey : lruHashMap.keySet()) {
            if (!protocols.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if (delKeys.size() > 0) {
            for (RpcProtocol delKey : delKeys) {
                lruHashMap.remove(delKey);
            }
        }

        // load
        RpcProtocol eldestKey = lruHashMap.entrySet().iterator().next().getKey();
        RpcProtocol eldestValue = lruHashMap.get(eldestKey);
        return eldestValue;
    }
}
