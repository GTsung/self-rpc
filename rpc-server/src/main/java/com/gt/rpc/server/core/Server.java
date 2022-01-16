package com.gt.rpc.server.core;

/**
 * @author GTsung
 * @date 2022/1/16
 */
public abstract class Server {

    /**
     * server start
     * @throws Exception
     */
    public abstract void start() throws Exception;

    /**
     * server stop
     * @throws Exception
     */
    public abstract void stop() throws Exception;
}
