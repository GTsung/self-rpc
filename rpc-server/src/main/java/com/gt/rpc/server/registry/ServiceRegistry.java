package com.gt.rpc.server.registry;

import com.gt.rpc.config.ZkConstant;
import com.gt.rpc.protocol.RpcProtocol;
import com.gt.rpc.protocol.RpcServiceInfo;
import com.gt.rpc.util.ServiceUtil;
import com.gt.rpc.zookeeper.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.state.ConnectionState;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * register service
 *
 * @author GTsung
 * @date 2022/1/16
 */
@Slf4j
public class ServiceRegistry {

    private final CuratorClient curatorClient;

    private final List<String> pathList = new CopyOnWriteArrayList<>();

    public ServiceRegistry(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress, 5000);
    }

    public void registerService(String host, int port, Map<String, Object> serviceMap) {
        // get serviceInfo
        List<RpcServiceInfo> serviceInfos = serviceMap.keySet().stream().map(key -> {
            String[] serviceName = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
            if (serviceName.length == 0) {
                log.warn("cannot register...");
                return null;
            }
            RpcServiceInfo serviceInfo = new RpcServiceInfo();
            serviceInfo.setServiceName(serviceName[0]);
            serviceInfo.setVersion(serviceName.length == 2 ? serviceName[1] : "");
            return serviceInfo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfoList(serviceInfos);
            String serviceData = rpcProtocol.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = ZkConstant.ZK_DATA_PATH + "-" + rpcProtocol.hashCode();
            path = curatorClient.createPathData(path, bytes);
            pathList.add(path);
            log.info("register {} new Service, host:{}, port:{}", serviceInfos.size(), host, port);
        } catch (Exception e) {
            log.info("register service failed", e);
        }

        // reConnected
        curatorClient.addConnectionStateListener((curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                log.info("Connection state: {}, register service after reconnected", connectionState);
                registerService(host, port, serviceMap);
            }
        });

    }

    public void unRegisterService() {
        log.info("Unregister all services");
        pathList.forEach(path -> {
            try {
                this.curatorClient.deletePath(path);
            } catch (Exception e) {
                log.info("delete {} service error", path);
            }
        });
        this.curatorClient.close();
    }
}
