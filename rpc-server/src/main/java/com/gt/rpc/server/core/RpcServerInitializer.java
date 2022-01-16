package com.gt.rpc.server.core;

import com.gt.rpc.codec.*;
import com.gt.rpc.serializer.Serializer;
import com.gt.rpc.serializer.kryo.KryoSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author GTsung
 * @date 2022/1/16
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

    // serviceMap:serviceKey serviceBean
    private Map<String, Object> handlerMap;
    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServerInitializer(Map<String, Object> handlerMap, ThreadPoolExecutor threadPoolExecutor) {
        this.handlerMap = handlerMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 指定序列化，粘包分包處理，添加handler
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));

//        Serializer serializer = ProtostuffSerializer.class.newInstance();
//        Serializer serializer = HessianSerializer.class.newInstance();
        Serializer serializer = KryoSerializer.class.newInstance();
        pipeline.addLast(new RpcDecoder(RpcRequest.class, serializer));
        pipeline.addLast(new RpcEncoder(RpcResponse.class, serializer));
        pipeline.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));
    }
}
