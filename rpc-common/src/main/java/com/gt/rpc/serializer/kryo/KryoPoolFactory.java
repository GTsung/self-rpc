package com.gt.rpc.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.gt.rpc.codec.RpcRequest;
import com.gt.rpc.codec.RpcResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KryoPoolFactory {

    private static class KryoPoolInstance {
        private static final KryoPool pool = new KryoPool.Builder(() -> {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.register(RpcRequest.class);
            kryo.register(RpcResponse.class);
            Kryo.DefaultInstantiatorStrategy strategy = (Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
            strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }).build();
    }

    public static KryoPool getKryoPool() {
        return KryoPoolInstance.pool;
    }

}
