package com.gt.rpc.test.service;

import com.gt.rpc.annotation.NettyRpcService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GTsung
 * @date 2022/1/21
 */
@NettyRpcService(PersonService.class)
public class PersonServiceImpl implements PersonService {

    @Override
    public List<Person> callPerson(String name, Integer num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(Integer.toString(i), name));
        }
        return persons;
    }
}
