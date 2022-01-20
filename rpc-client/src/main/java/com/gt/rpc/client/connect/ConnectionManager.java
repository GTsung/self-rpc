package com.gt.rpc.client.connect;

import com.gt.rpc.client.handler.RpcClientHandler;
import com.gt.rpc.client.handler.RpcClientInitializer;
import com.gt.rpc.client.route.RpcLoadBalance;
import com.gt.rpc.client.route.RpcLoadBalanceRoundRobin;
import com.gt.rpc.protocol.RpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectionManager {

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8,
            600L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));

    // 維護handler
    private Map<RpcProtocol, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<RpcProtocol> rpcProtocolSet = new CopyOnWriteArraySet<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private long waitTimeOut = 5000;
    private RpcLoadBalance loadBalance = new RpcLoadBalanceRoundRobin();
    private volatile boolean isRunning = true;

    private static class SingletonHolder {
        private static final ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 更新本地服務及連接
     *
     * @param rpcProtocols
     */
    public void updateConnectedServer(List<RpcProtocol> rpcProtocols) {
        if (!CollectionUtils.isEmpty(rpcProtocols)) {
            Set<RpcProtocol> serviceSet = new HashSet<>(rpcProtocols);
            for (final RpcProtocol rpcProtocol : serviceSet) {
                if (!rpcProtocolSet.contains(rpcProtocol)) {
                    // 開始連接服務
                    connectServer(rpcProtocol);
                }
            }
            // 關閉且刪除無效服務節點
            for (RpcProtocol rpcProtocol : rpcProtocolSet) {
                if (!serviceSet.contains(rpcProtocol)) {
                    log.info("remove invalid service" + rpcProtocol.toJson());
                    removeAndCloseHandler(rpcProtocol);
                }
            }
        } else {
            log.info("no available service");
            for (RpcProtocol rpcProtocol : rpcProtocolSet) {
                removeAndCloseHandler(rpcProtocol);
            }
        }
    }

    // 移除失效服務，關閉連接
    private void removeAndCloseHandler(RpcProtocol rpcProtocol) {
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            handler.close();
        }
        connectedServerNodes.remove(rpcProtocol);
        rpcProtocolSet.remove(rpcProtocol);
    }

    // 連接新服務
    private void connectServer(RpcProtocol rpcProtocol) {
        if (CollectionUtils.isEmpty(rpcProtocol.getServiceInfoList())) {
            log.info("No service, host:{}, port:{}", rpcProtocol.getHost(), rpcProtocol.getPort());
            return;
        }
        // 加入緩存并且連接
        rpcProtocolSet.add(rpcProtocol);
        log.info("new Service, host:{},port:{}", rpcProtocol.getHost(), rpcProtocol.getPort());
        rpcProtocol.getServiceInfoList().forEach(rpcServiceInfo -> {
            log.info("New Service Info, name : {}, version: {}", rpcServiceInfo.getServiceName(), rpcServiceInfo.getVersion());
        });
        final InetSocketAddress remoteAddr = new InetSocketAddress(rpcProtocol.getHost(), rpcProtocol.getPort());
        // 利用綫程池進行連接
        threadPoolExecutor.submit(() -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer());
            ChannelFuture future = bootstrap.connect(remoteAddr);
            future.addListener((final ChannelFuture channelFuture) -> {
                if (channelFuture.isSuccess()) {
                    // 連接成功
                    log.info("connect remoteAddr success, remoteAddr:{}", remoteAddr);
                    RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                    // 緩存handler
                    connectedServerNodes.put(rpcProtocol, handler);
                    handler.setRpcProtocol(rpcProtocol);
                    // 通知
                    signalAvailableHandler();
                } else {
                    log.info("cannot connect remoteAddr:{}", remoteAddr);
                }
            });
        });
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // 通過類型更新
    public void updateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {
        if (Objects.isNull(rpcProtocol)) return;
        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !rpcProtocolSet.contains(rpcProtocol)) {
            connectServer(rpcProtocol);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            removeAndCloseHandler(rpcProtocol);
            connectServer(rpcProtocol);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            removeAndCloseHandler(rpcProtocol);
        } else {
            throw new IllegalArgumentException("unknown type " + type);
        }
    }

    /**
     * 獲取handler
     *
     * @param serviceKey
     * @return
     * @throws Exception
     */
    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        // 連接的handler數量
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0) {
            // 等待連接
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                log.info("waiting for available service is interrupted", e);
            }
        }
        RpcProtocol rpcProtocol = loadBalance.route(serviceKey, connectedServerNodes);
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            return handler;
        } else {
            throw new Exception("Can not get available connection");
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            log.warn("waiting for available service");
            return connected.await(this.waitTimeOut, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void removeHandler(RpcProtocol rpcProtocol) {
        rpcProtocolSet.remove(rpcProtocol);
        connectedServerNodes.remove(rpcProtocol);
        log.info("Remove one connection, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
    }

    public void stop() {
        isRunning = false;
        for (RpcProtocol rpcProtocol : rpcProtocolSet) {
            removeAndCloseHandler(rpcProtocol);
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
