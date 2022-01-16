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
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 8215493329459772524L;

    /**
     * 區分客戶端
     */
    private String requestId;

    /**
     * 調用錯誤信息
     */
    private String error;

    /**
     * 調用結果
     */
    private Object result;

    public boolean isError() {
        return error != null;
    }
}
