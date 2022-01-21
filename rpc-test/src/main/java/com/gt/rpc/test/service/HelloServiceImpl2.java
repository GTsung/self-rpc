package com.gt.rpc.test.service;

import com.gt.rpc.annotation.NettyRpcService;

/**
 * @author GTsung
 * @date 2022/1/21
 */
@NettyRpcService(value = HelloService.class, version = "2.0")
public class HelloServiceImpl2 implements HelloService {

    @Override
    public String hello(String name) {
        return "Hi " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hi " + person.getFirstName() + " " + person.getLastName();
    }

    @Override
    public String hello(String name, Integer age) {
        return name + " is " + age + " years old";
    }
}
