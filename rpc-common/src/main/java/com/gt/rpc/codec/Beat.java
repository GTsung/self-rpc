package com.gt.rpc.codec;

/**
 * 檢測心跳
 *
 * @author GTsung
 * @date 2022/1/16
 */
public final class Beat {

    public static final int BEAT_INTERVAL = 30;
    public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;
    public static final String BEAT_ID = "BEAT_PING_PONG";
    public static RpcRequest BEAT_PING;

    static {
        BEAT_PING = new RpcRequest() {
        };
        BEAT_PING.setRequestId(BEAT_ID);
    }

}
