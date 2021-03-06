package com.gt.rpc.serializer;

/**
 * εΊεεε¨
 *
 * @author GTsung
 * @date 2022/1/16
 */
public abstract class Serializer {

    public abstract <T> byte[] serialize(T obj);

    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);

}
