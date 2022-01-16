package com.gt.rpc.client.discovery;

import com.gt.rpc.config.ZkConstant;
import com.gt.rpc.protocol.RpcProtocol;
import com.gt.rpc.zookeeper.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@Slf4j
public class ServiceDiscovery {

    private CuratorClient curatorClient;

    public ServiceDiscovery(String registerAddress) {
        this.curatorClient = new CuratorClient(registerAddress);
        discoveryService();
    }

    private void discoveryService() {
        try {
            log.info("get initial service info");
            // 獲取service
            getServiceAndUpdateServer();
            // watch
            curatorClient.watchPathChildrenNode(ZkConstant.ZK_REGISTRY_PATH,
                    (curatorFramework, pathChildrenCacheEvent) -> {
                        PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                        ChildData childData = pathChildrenCacheEvent.getData();
                        switch (type) {
                            case CONNECTION_RECONNECTED:
                                log.info("Reconnected to zk, try to get latest service list");
                                getServiceAndUpdateServer();
                                break;
                            case CHILD_ADDED:
                                getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                                break;
                            case CHILD_UPDATED:
                                getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                                break;
                            case CHILD_REMOVED:
                                getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                                break;
                        }
                    });
        } catch (Exception e) {
            log.info("watch node exception: ", e);
        }
    }

    private void getServiceAndUpdateServer() {
        try {
            List<String> nodeList = curatorClient.getChildren(ZkConstant.ZK_REGISTRY_PATH);
            List<RpcProtocol> rpcProtocols = new ArrayList<>();
            for (String node : nodeList) {
                log.debug("service node : {}", node);
                byte[] bytes = curatorClient.getData(ZkConstant.ZK_REGISTRY_PATH + "/" + node);
                rpcProtocols.add(RpcProtocol.fromJson(new String(bytes)));
            }
            log.debug("service node data: {}", rpcProtocols);
            // update
            updateConnectedServer(rpcProtocols);
        } catch (Exception e) {
            log.info("get node exception: ", e);
        }

    }

    private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        String data = new String(childData.getData(), StandardCharsets.UTF_8);
        log.info("child data updated, path: {} , type: {} , data: {}", path, type, data);
        RpcProtocol rpcProtocol = RpcProtocol.fromJson(data);
        updateConnectedServer(rpcProtocol, type);
    }

    private void updateConnectedServer(List<RpcProtocol> rpcProtocols) {

    }

    private void updateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {

    }

    public void stop() {
        this.curatorClient.close();
    }
}
