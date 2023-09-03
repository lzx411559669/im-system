package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.reviver.UserLoginMessageListenner;
import org.redisson.api.RedissonClient;

public class RedisManager {
    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config){
        loginModel = config.getLim().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLim().getRedis());
        //TODO userLoginMessageListener
        UserLoginMessageListenner userLoginMessageListenner = new UserLoginMessageListenner(loginModel);
        userLoginMessageListenner.listenUserLogin();

    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }
}
