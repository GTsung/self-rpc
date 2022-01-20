package com.gt.rpc.client.proxy;

import com.gt.rpc.client.connect.ConnectionManager;
import com.gt.rpc.client.handler.RpcClientHandler;
import com.gt.rpc.client.handler.RpcFuture;
import com.gt.rpc.codec.RpcRequest;
import com.gt.rpc.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@Slf4j
public class ObjectProxy<T, P> implements InvocationHandler, RpcService<T, P, SerializableFunction<T>> {

    private Class<T> clazz;

    private String version;

    public ObjectProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);
        // Debug
        if (log.isDebugEnabled()) {
            log.debug(method.getDeclaringClass().getName());
            log.debug(method.getName());
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                log.debug(method.getParameterTypes()[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                log.debug(args[i].toString());
            }
        }

        String serviceKey = ServiceUtil.makeServiceKey(method.getDeclaringClass().getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture.get();
    }

    @Override
    public RpcFuture call(String funcName, Object... args) throws Exception {
        String serviceKey = ServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
        return handler.sendRequest(request);
    }

    @Override
    public RpcFuture call(SerializableFunction<T> fn, Object... args) throws Exception {
        String serviceKey = ServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = createRequest(this.clazz.getName(), fn.getName(), args);
        return handler.sendRequest(request);
    }

    private RpcRequest createRequest(String className, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(version);
        Class[] parameterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);

        // Debug
        if (log.isDebugEnabled()) {
            log.debug(className);
            log.debug(methodName);
            for (int i = 0; i < parameterTypes.length; ++i) {
                log.debug(parameterTypes[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                log.debug(args[i].toString());
            }
        }

        return request;
    }

    private Class<?> getClassType(Object obj) {
        return obj.getClass();
    }
}
