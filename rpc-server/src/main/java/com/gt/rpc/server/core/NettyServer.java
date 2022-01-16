package com.gt.rpc.server.core;

import com.gt.rpc.server.registry.ServiceRegistry;
import com.gt.rpc.util.ServiceUtil;
import com.gt.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@Slf4j
public class NettyServer extends Server {

    private Thread thread;
    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public NettyServer(String serverAddress, String registryAddress) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = new ServiceRegistry(registryAddress);
    }

    public void addService(String interfaceName, String version, Object serviceBean) {
        log.info("adding service... interfaceName:{}, version:{}, serviceBean:{}",
                interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
    }

    @Override
    public void start() throws Exception {
        thread = new Thread(new Runnable() {
            ThreadPoolExecutor threadPoolExecutor =
                    ThreadPoolUtil.makeServerThreadPool(
                            NettyServer.class.getSimpleName(), 16, 32);

            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new RpcServerInitializer(serviceMap, threadPoolExecutor))
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);

                    ChannelFuture channelFuture = bootstrap.bind(host, port).sync();

                    if (serviceRegistry != null) {
                        serviceRegistry.registerService(host, port, serviceMap);
                    }
                    log.info("server started on port {}", port);
                    channelFuture.channel().closeFuture().sync();
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        log.info("rpc server remoting server stop");
                    } else {
                        log.info("rpc server remoting server error", e);
                    }
                } finally {
                    try {
                        serviceRegistry.unRegisterService();
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception ex) {
                        log.info(ex.getMessage(), ex);
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        if (thread != null && thread.isAlive())
            thread.interrupt();
    }
}
