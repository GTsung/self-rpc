package com.gt.rpc.serializer;

/**
 * 序列化器
 *
 * @author GTsung
 * @date 2022/1/16
 */
public abstract class Serializer {

    public abstract <T> byte[] serialize(T obj);

    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);

}
