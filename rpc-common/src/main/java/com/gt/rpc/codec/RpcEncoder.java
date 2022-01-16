package com.gt.rpc.codec;

import com.gt.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 編碼
 *
 * @author GTsung
 * @date 2022/1/16
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;
    private Serializer serializer;

    public RpcEncoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        // Class.isInstance(obj) ---> obj是否可以轉換為這個Class對象的實例
        if (genericClass.isInstance(in)) {
            try {
                byte[] bytes = serializer.serialize(in);
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
            } catch (Exception e) {
                log.info("Encode error, {}", e.toString());
            }
        }
    }
}
