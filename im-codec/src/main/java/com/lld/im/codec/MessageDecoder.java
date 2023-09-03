package com.lld.im.codec;

import com.lld.im.codec.proto.Message;
import com.lld.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
/**
 * @description: 消息解码类
 * @author: lzx
 * @version: 1.0
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes()<28){
            return;
        }
        Message message = ByteBufToMessageUtils.transition(in);
        if (message == null) {
           return;
        }
        out.add(message);
    }
}
