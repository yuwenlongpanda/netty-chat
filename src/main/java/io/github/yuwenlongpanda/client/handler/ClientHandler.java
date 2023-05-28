package io.github.yuwenlongpanda.client.handler;

import io.github.yuwenlongpanda.pojo.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
    CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
    AtomicBoolean LOGIN = new AtomicBoolean(false);
    AtomicBoolean EXIT = new AtomicBoolean(false);
    Scanner scanner = new Scanner(System.in);

    // 接收响应消息
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("msg: {}", msg);
        if ((msg instanceof LoginResponseMessage)) {
            LoginResponseMessage response = (LoginResponseMessage) msg;
            if (response.isSuccess()) {
                // 如果登录成功
                LOGIN.set(true);
            }
            // 唤醒 system in 线程
            WAIT_FOR_LOGIN.countDown();
        }
    }

    // 在连接建立后触发 active 事件
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 负责接收用户在控制台的输入，负责向服务器发送各种消息
        new Thread(() -> {
            System.out.println("请输入用户名:");
            String username = scanner.nextLine();
            if (EXIT.get()) {
                return;
            }
            System.out.println("请输入密码:");
            String password = scanner.nextLine();
            if (EXIT.get()) {
                return;
            }
            // 构造消息对象
            LoginRequestMessage message = new LoginRequestMessage(username, password);
            System.out.println(message);
            // 发送消息
            ctx.writeAndFlush(message);
            System.out.println("等待后续操作...");
            try {
                WAIT_FOR_LOGIN.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 如果登录失败
            if (!LOGIN.get()) {
                ctx.channel().close();
                return;
            }
            while (true) {
                System.out.println("==================================");
                System.out.println("send [username] [content]");
                System.out.println("gsend [group name] [content]");
                System.out.println("gcreate [group name] [m1,m2,m3...]");
                System.out.println("gmembers [group name]");
                System.out.println("gjoin [group name]");
                System.out.println("gquit [group name]");
                System.out.println("quit");
                System.out.println("==================================");
                String command = null;
                try {
                    command = scanner.nextLine();
                } catch (Exception e) {
                    break;
                }
                if (EXIT.get()) {
                    return;
                }
                String[] s = command.split(" ");
                switch (s[0]) {
                    case "send":
                        ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                        break;
                    case "gsend":
                        ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                        break;
                    case "gcreate":
                        Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                        set.add(username); // 加入自己
                        ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                        break;
                    case "gmembers":
                        ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                        break;
                    case "gjoin":
                        ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                        break;
                    case "gquit":
                        ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                        break;
                    case "quit":
                        ctx.channel().close();
                        return;
                }
            }
        }, "system in").start();
    }


    // 在连接断开时触发
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("连接已经断开，按任意键退出..");
        EXIT.set(true);
    }

    // 在出现异常时触发
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("连接已经断开，按任意键退出..{}", cause.getMessage());
        EXIT.set(true);
    }
}