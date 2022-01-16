package com.gt.rpc.serializer.protostuff;

import com.gt.rpc.serializer.Serializer;
import com.gt.rpc.util.ProtoJsonUtil;

/**
 * @author GTsung
 * @date 2022/1/16
 */
public class ProtostuffSerializer extends Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        return ProtoJsonUtil.serialize(obj);
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        return ProtoJsonUtil.deserialize(bytes, clazz);
    }
}
