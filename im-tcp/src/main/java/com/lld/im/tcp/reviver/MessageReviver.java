package com.lld.im.tcp.reviver;

import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j
public class MessageReviver {
    private static String brokerId;

    private static void startReciverMessage(){
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im+brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im+brokerId, true,
                    false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im+brokerId,
                    Constants.RabbitConstants.MessageService2Im, brokerId);
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im+brokerId,new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    log.info("收到消息:{}",new String(body));
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void init(){
        startReciverMessage();
    }

    public static void init(String brokerId){
        if (StringUtils.isBlank(MessageReviver.brokerId)){
            MessageReviver.brokerId = brokerId;
        }
        startReciverMessage();
    }
}
