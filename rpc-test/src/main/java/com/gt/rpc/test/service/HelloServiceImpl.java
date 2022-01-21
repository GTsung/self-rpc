package com.gt.rpc.test.service;

import com.gt.rpc.annotation.NettyRpcService;

/**
 * @author GTsung
 * @date 2022/1/21
 */
@NettyRpcService(value = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello " + person.getFirstName() + " " + person.getLastName();
    }

    @Override
    public String hello(String name, Integer age) {
        return name + " is " + age;
    }
}
