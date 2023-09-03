package com.lld.im.tcp.handler;

import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private Long heartBeatTime;

    public HeartBeatHandler(Long heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("读空闲");
            }
            if (event.state() == IdleState.WRITER_IDLE) {
                log.info("写空闲");
            }
            if (event.state() == IdleState.ALL_IDLE) {
                Long lastReadTime = (Long)ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).get();

                Long now = System.currentTimeMillis();

                if (lastReadTime!=null && (now - lastReadTime) > heartBeatTime) {
                    //TODO 退后台逻辑
                    SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());

                }
            }
        }
    }
}
