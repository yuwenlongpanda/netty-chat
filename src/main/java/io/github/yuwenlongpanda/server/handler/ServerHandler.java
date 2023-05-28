package io.github.yuwenlongpanda.server.handler;

import io.github.yuwenlongpanda.pojo.*;
import io.github.yuwenlongpanda.server.service.im.ChatService;
import io.github.yuwenlongpanda.server.service.im.GroupService;
import io.github.yuwenlongpanda.server.service.im.LoginService;
import io.github.yuwenlongpanda.server.service.im.ServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        if (message instanceof LoginRequestMessage) {
            ServiceFactory.getService(LoginService.class).handle(channelHandlerContext, (LoginRequestMessage) message);
        } else if (message instanceof ChatRequestMessage) {
            ServiceFactory.getService(ChatService.class).handle(channelHandlerContext, (ChatRequestMessage) message);
        } else if (message instanceof GroupCreateRequestMessage) {
            ServiceFactory.getService(GroupService.class).createGroup(channelHandlerContext, (GroupCreateRequestMessage) message);
        } else if (message instanceof GroupJoinRequestMessage) {
            ServiceFactory.getService(GroupService.class).joinGroup(channelHandlerContext, (GroupJoinRequestMessage) message);
        } else if (message instanceof GroupMembersRequestMessage) {
            ServiceFactory.getService(GroupService.class).groupMembers(channelHandlerContext, (GroupMembersRequestMessage) message);
        } else if (message instanceof GroupQuitRequestMessage) {
            ServiceFactory.getService(GroupService.class).quitGroup(channelHandlerContext, (GroupQuitRequestMessage) message);
        } else if (message instanceof GroupChatRequestMessage) {
            ServiceFactory.getService(GroupService.class).chatGroup(channelHandlerContext, (GroupChatRequestMessage) message);
        }
    }
}