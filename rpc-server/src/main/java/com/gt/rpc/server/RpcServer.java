package com.gt.rpc.server;

import com.gt.rpc.annotation.NettyRpcService;
import com.gt.rpc.server.core.NettyServer;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author GTsung
 * @date 2022/1/16
 */
public class RpcServer extends NettyServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    public RpcServer(String serverAddress, String registryAddress) {
        super(serverAddress, registryAddress);
    }

    @Override
    public void destroy() throws Exception {
        super.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(NettyRpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            serviceBeanMap.values().forEach(serviceBean -> {
                NettyRpcService nettyRpcService = serviceBean.getClass().getAnnotation(NettyRpcService.class);
                String interfaceName = nettyRpcService.value().getName();
                String version = nettyRpcService.version();
                super.addService(interfaceName, version, serviceBean);
            });
        }
    }
}
