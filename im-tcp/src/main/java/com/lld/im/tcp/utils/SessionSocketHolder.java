package com.lld.im.tcp.utils;

import com.alibaba.fastjson.JSON;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionSocketHolder {
    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(Integer appId,String userId,Integer clientType,
                           String imei,NioSocketChannel channel){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setUserId(userId);
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        CHANNELS.put(userClientDto,channel);
    }

    public static NioSocketChannel get(Integer appId,String userId,Integer clientType,
                                       String imei){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setUserId(userId);
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);

        return CHANNELS.get(userClientDto);
    }

    public static List<NioSocketChannel> get(Integer appId , String id) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel ->{
            if(channel.getAppId().equals(appId) && id.equals(channel.getUserId())){
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;
    }

    public static void remove(Integer appId,String userId,Integer clientType,
                              String imei){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setImei(imei);
        userClientDto.setUserId(userId);
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);

        CHANNELS.remove(userClientDto);

    }

    public static void remove(NioSocketChannel channel){
        CHANNELS.entrySet().stream().filter(entry -> entry.getValue().equals(channel)).forEach(entry -> {
            CHANNELS.remove(entry.getKey());
        });
    }

    public static void removeUserSession(NioSocketChannel nioSocketChannel){
        String userId = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        String imei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        Integer clientType = (Integer)nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        Integer appId = (Integer)nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        SessionSocketHolder.remove(appId,userId,clientType,imei);

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType + ":" + imei);
        nioSocketChannel.close();
    }

    public static  void offlineUserSession(NioSocketChannel nioSocketChannel){
        String userId = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        String imei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        Integer clientType = (Integer)nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        Integer appId = (Integer)nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        SessionSocketHolder.remove(appId,userId,clientType,imei);

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        String sesstionStr = map.get(clientType.toString() + ":" + imei);
        if (!StringUtils.isNotBlank(sesstionStr)){
            UserSession userSession = JSON.parseObject(sesstionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType.toString() + ":" + imei, JSON.toJSONString(userSession));
        }
        nioSocketChannel.close();
    }

}
