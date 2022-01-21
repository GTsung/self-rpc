package com.gt.rpc.client.route.impl;

import com.gt.rpc.client.route.RpcLoadBalance;
import com.gt.rpc.protocol.RpcProtocol;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author GTsung
 * @date 2022/1/21
 */
public class RpcLoadBalanceLFU extends RpcLoadBalance {

    private ConcurrentMap<String, HashMap<RpcProtocol, Integer>> jobLfuMap = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    @Override
    protected RpcProtocol doRoute(String serviceKey, List<RpcProtocol> protocols) {
        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLfuMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }
        // lfu item init
        HashMap<RpcProtocol, Integer> lfuItemMap = jobLfuMap.get(serviceKey);
        if (lfuItemMap == null) {
            lfuItemMap = new HashMap<>();
            jobLfuMap.putIfAbsent(serviceKey, lfuItemMap);   // 避免重复覆盖
        }
        // put new
        for (RpcProtocol address : protocols) {
            if (!lfuItemMap.containsKey(address) || lfuItemMap.get(address) > 1000000) {
                lfuItemMap.put(address, 0);
            }
        }
        // remove old
        List<RpcProtocol> delKeys = lfuItemMap.keySet()
                .stream()
                .filter(existKey -> !protocols.contains(existKey))
                .collect(Collectors.toList());
        if (delKeys.size() > 0) {
            for (RpcProtocol delKey : delKeys) {
                lfuItemMap.remove(delKey);
            }
        }

        // load least used count address
        List<Map.Entry<RpcProtocol, Integer>> lfuItemList = new ArrayList<>(lfuItemMap.entrySet());
        Collections.sort(lfuItemList, new Comparator<Map.Entry<RpcProtocol, Integer>>() {
            @Override
            public int compare(Map.Entry<RpcProtocol, Integer> o1, Map.Entry<RpcProtocol, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        Map.Entry<RpcProtocol, Integer> addressItem = lfuItemList.get(0);
        RpcProtocol minAddress = addressItem.getKey();
        addressItem.setValue(addressItem.getValue() + 1);
        return minAddress;
    }
}
