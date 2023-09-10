package com.lld.im.service.config;

import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.ImUrlRouteWayEnum;
import com.lld.im.common.enums.RouteHashMethodEnum;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class BeanConfig {
    @Autowired
    private AppConfig appConfig;

    @Bean
    public ZkClient zkClient(){
        return new ZkClient(appConfig.getZkAddr(),appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle routeHandle() throws  Exception {
        Integer imRouteWay = appConfig.getImRouteWay();
        String routeWay = "";
        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routeWay = handler.getClazz();
        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();
        if (handler == ImUrlRouteWayEnum.HASH){
            Method setHash = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay = "";
            RouteHashMethodEnum hashHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
            hashWay = hashHandler.getClazz();
            AbstractConsistentHash consistentHash = (AbstractConsistentHash) Class.forName(hashWay).newInstance();
            setHash.invoke(routeHandle,consistentHash);
        }
        return routeHandle;
    }

}
