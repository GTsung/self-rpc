package com.gt.rpc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadPoolUtil {

    public static ThreadPoolExecutor makeServerThreadPool(final String serviceName,
                                                          int corePoolSize, int maxPoolSize) {
        return new ThreadPoolExecutor(corePoolSize,
                maxPoolSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> new Thread(r, "netty-rpc-" + serviceName + "-" + r.hashCode()),
                new ThreadPoolExecutor.AbortPolicy());
    }

}
