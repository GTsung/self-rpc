package com.gt.rpc.codec;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@Getter
@Setter
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -2524587347775862771L;

    /**
     * 區分哪一個客戶端的請求
     */
    private String requestId;

    /**
     * 調用服務端的哪一個接口
     */
    private String className;

    /**
     * 調用服務端的哪一個方法
     */
    private String methodName;

    /**
     * 調用服務端的方法的參數類型
     */
    private Class<?>[] parameterTypes;

    /**
     * 傳遞的參數值
     */
    private Object[] parameters;

    /**
     * 版本號
     */
    private String version;
}
