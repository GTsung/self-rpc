package com.gt.rpc.test.service;

import lombok.*;

import java.io.Serializable;

/**
 * @author GTsung
 * @date 2022/1/21
 */
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Serializable {

    private static final long serialVersionUID = -3475626311941868983L;
    private String firstName;
    private String lastName;
}
