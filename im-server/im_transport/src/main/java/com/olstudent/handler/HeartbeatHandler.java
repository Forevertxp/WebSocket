package com.olstudent.handler;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.connection.ConnPool;
import com.olstudent.protocol.MessageHolder;
import com.olstudent.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 心跳检测Handler
 * <p>
 *
 * @author txp.
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(HeartbeatHandler.class);

    public static AtomicBoolean isLogout = new AtomicBoolean(false);

    private Channel channel;
    private String username;

    // 丢失的心跳数
    private int counter = 0;

    public HeartbeatHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (username == null) {
                username = ConnPool.query(channel);
            }
            // 心跳丢失
            counter++;
            logger.info(username + " 丢失" + counter + "个心跳包");
            if (counter > 4) {
                // 心跳丢失数达到5个，主动断开连接
                ctx.channel().close();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnPool.remove(username);
        if (isLogout.get()) {
            isLogout.set(false);
            logger.info(username + " 退出登录");
        } else {
            logger.info(username + " 与服务器断开连接");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            String message = ((TextWebSocketFrame) msg).text();
            MessageHolder messageHolder = JSONObject.parseObject(message,MessageHolder.class);
            if (messageHolder.getType() == ProtocolHeader.HEARTBEAT) {
                if (username == null) {
                    username = ConnPool.query(channel);
                }
                // 心跳丢失清零
                counter = 0;
//                logger.info(username + " 收到心跳包");
                ReferenceCountUtil.release(msg);
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }
}