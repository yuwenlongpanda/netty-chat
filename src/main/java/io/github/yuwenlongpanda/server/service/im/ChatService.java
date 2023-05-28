package io.github.yuwenlongpanda.server.service.im;

import io.github.yuwenlongpanda.pojo.ChatRequestMessage;
import io.github.yuwenlongpanda.pojo.ChatResponseMessage;
import io.github.yuwenlongpanda.server.service.business.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ChatService {

    public void handle(ChannelHandlerContext ctx, ChatRequestMessage msg) {
        String to = msg.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        // 在线
        if (channel != null) {
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
        // 不在线
        else {
            ctx.writeAndFlush(new ChatResponseMessage(false, "对方用户不存在或者不在线"));
        }
    }
}
