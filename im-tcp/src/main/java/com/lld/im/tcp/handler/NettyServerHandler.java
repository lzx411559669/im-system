package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.LoginPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private Integer brokerId;

    public NettyServerHandler(Integer brokerId){
        this.brokerId = brokerId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Integer command = msg.getMessageHeader().getCommand();
        //登陆
        if (command == SystemCommand.LOGIN.getCommand()){
            Object messagePack = msg.getMessagePack();
            LoginPack loginPack = JSON.parseObject(JSON.toJSONString(messagePack), new TypeReference<LoginPack>() {
            }.getType());

            String userId = loginPack.getUserId();

            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(userId);
            String clientImei = msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei();
            /** 为channel设置client和imel **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientImei)).set(clientImei);
            /** 为channel设置appId **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
            /** 为channel设置ClientType **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType))
                    .set(msg.getMessageHeader().getClientType());
            /** 为channel设置Imei **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei))
                    .set(msg.getMessageHeader().getImei());
            //将channel存起来
            UserSession userSession = new UserSession();
            userSession.setUserId(userId);
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setImei(msg.getMessageHeader().getImei());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            }catch (Exception e){
                e.printStackTrace();
            }

            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<Object, Object> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + userId);
            map.put(msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei(), JSON.toJSONString(userSession));

            SessionSocketHolder
                    .put(msg.getMessageHeader().getAppId(), userId,
                            msg.getMessageHeader().getClientType(),
                            msg.getMessageHeader().getImei(),
                            (NioSocketChannel) ctx.channel());

            //使用redis发布订阅处理多端登录问题
            UserClientDto dto = new UserClientDto();
            dto.setUserId(userId);
            dto.setAppId(msg.getMessageHeader().getAppId());
            dto.setClientType(msg.getMessageHeader().getClientType());
            dto.setImei(msg.getMessageHeader().getImei());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSON.toJSONString(dto));

        }else if (command == SystemCommand.LOGOUT.getCommand()){
            //退出
            //删除session
            //redis删除
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        }else if (command == SystemCommand.PING.getCommand()){
            //记录最后一次时间
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }

        System.out.println(msg.toString());
    }
}
