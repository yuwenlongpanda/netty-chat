package io.github.yuwenlongpanda.server.service.im;

import io.github.yuwenlongpanda.pojo.LoginRequestMessage;
import io.github.yuwenlongpanda.pojo.LoginResponseMessage;
import io.github.yuwenlongpanda.server.service.UserServiceFactory;
import io.github.yuwenlongpanda.server.session.SessionFactory;
import io.netty.channel.ChannelHandlerContext;

public class LoginService {

    public void handle(ChannelHandlerContext ctx, LoginRequestMessage msg) {
        String username = msg.getUsername();
        String password = msg.getPassword();
        boolean login = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage message;
        if (login) {
            SessionFactory.getSession().bind(ctx.channel(), username);
            message = new LoginResponseMessage(true, "登录成功");
        } else {
            message = new LoginResponseMessage(false, "用户名或密码不正确");
        }
        ctx.writeAndFlush(message);
    }
}
