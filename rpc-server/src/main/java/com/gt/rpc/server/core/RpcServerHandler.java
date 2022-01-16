package com.gt.rpc.server.core;

import com.gt.rpc.codec.Beat;
import com.gt.rpc.codec.RpcRequest;
import com.gt.rpc.codec.RpcResponse;
import com.gt.rpc.util.ServiceUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * rpcRequest Handler
 *
 * @author GTsung
 * @date 2022/1/16
 */
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;
    private final ThreadPoolExecutor serverHandlerPool;

    public RpcServerHandler(Map<String, Object> handlerMap, final ThreadPoolExecutor threadPoolExecutor) {
        this.handlerMap = handlerMap;
        this.serverHandlerPool = threadPoolExecutor;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final RpcRequest request) throws Exception {
        if (Beat.BEAT_ID.equalsIgnoreCase(request.getRequestId())) {
            log.info("server read heartbeat ping");
            return;
        }

        serverHandlerPool.execute(() -> {
            log.info("receive request: {}", request.getRequestId());
            // send response
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            try {
                response.setResult(handle(request));
            } catch (Throwable t) {
                response.setError(t.toString());
                log.info("rpc server handler request error");
            }
            ctx.writeAndFlush(response).addListener((channelFuture) -> {
                log.info("send response for request:{} finished", request.getRequestId());
            });
        });
    }

    private Object handle(RpcRequest request) throws Throwable {
        String serviceName = request.getClassName();
        String version = request.getVersion();

        String serviceKey = ServiceUtil.makeServiceKey(serviceName, version);
        Object serviceBean = handlerMap.get(serviceKey);
        if (Objects.isNull(serviceBean)) {
            log.info("cannot find service by interfaceName:{} and version:{}", serviceName, version);
            return null;
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        log.debug("service:{}, method:{}", serviceClass.getName(), methodName);
        Arrays.stream(parameterTypes).forEach(p -> log.debug("parameterType: {}", p.getName()));
        Arrays.stream(parameters).forEach(p -> log.debug("parameter: {}", p.toString()));

        // jdk reflect
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);

        // Cglib reflect
        FastClass serviceFastClass = FastClass.create(serviceClass);
        int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);
        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("server caught exception: " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            log.warn("channel idle in last {} seconds , close it", Beat.BEAT_TIMEOUT);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
