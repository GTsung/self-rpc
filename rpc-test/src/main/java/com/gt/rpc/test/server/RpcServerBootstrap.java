package com.gt.rpc.test.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author GTsung
 * @date 2022/1/21
 */
public class RpcServerBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("server-spring.xml");
    }
}
