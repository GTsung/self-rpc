package com.gt.rpc.client.proxy;

import com.gt.rpc.client.handler.RpcFuture;

public interface RpcService<T, P, I extends  SerializableFunction<T>> {

    RpcFuture call(String funcName, Object... args) throws Exception;

    /**
     * lambda method reference
     */
    RpcFuture call(I fn, Object... args) throws Exception;
}
