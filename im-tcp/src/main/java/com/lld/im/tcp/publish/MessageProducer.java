package com.lld.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageProducer {

    public static void sendMessage(Object message){
        Channel channel = null;
        String channelName = Constants.RabbitConstants.MessageService2Im;

        try {
            channel = MqFactory.getChannel(channelName);
            channel.basicPublish(channelName, "",null, JSON.toJSONString(message).getBytes());

        }catch (Exception e){
            e.printStackTrace();
            log.error("发送消息失败,{}",e.getMessage());
        }

    }
}
