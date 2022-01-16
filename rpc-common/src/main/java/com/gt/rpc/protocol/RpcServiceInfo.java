package com.gt.rpc.protocol;

import com.gt.rpc.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author GTsung
 * @date 2022/1/16
 */
@Getter
@Setter
public class RpcServiceInfo implements Serializable {

    // service-interface-name
    private String serviceName;

    // service-version
    private String version;

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RpcServiceInfo info = (RpcServiceInfo) obj;
        return Objects.equals(this.serviceName, info.serviceName)
                && Objects.equals(this.version, info.version);
    }

    public String toJson() {
        return JsonUtil.obj2Json(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
