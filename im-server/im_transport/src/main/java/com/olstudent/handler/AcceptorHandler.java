package com.olstudent.handler;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.protocol.MessageHolder;
import com.olstudent.protocol.ProtocolHeader;
import com.olstudent.queue.TaskQueue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * 最终接收数据的Handler，将待处理数据放入阻塞队列中，由服务模块take and deal.
 *
 * @author txp.
 */
public class AcceptorHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(AcceptorHandler.class);

    private final BlockingQueue<MessageHolder> taskQueue;

    private WebSocketServerHandshaker handshaker;

    public AcceptorHandler() {
        taskQueue = TaskQueue.getQueue();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //http：//xxxx
        if(msg instanceof FullHttpRequest){

            handleHttpRequest(ctx,(FullHttpRequest)msg);
        }else if(msg instanceof WebSocketFrame){
            //ws://xxxx
            handlerWebSocketFrame(ctx,(WebSocketFrame)msg);
        }
    }

    public void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception{

        //关闭请求
        if(frame instanceof CloseWebSocketFrame){

            handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());

            return;
        }
        //ping请求
        if(frame instanceof PingWebSocketFrame){

            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));

            return;
        }
        //只支持文本格式，不支持二进制消息
        if(!(frame instanceof TextWebSocketFrame)){

            throw new Exception("仅支持文本格式");
        }

        //客服端发送过来的消息
        String message = ((TextWebSocketFrame) frame).text();
        System.out.println("服务端收到：" + message);

        MessageHolder messageHolder = JSONObject.parseObject(message,MessageHolder.class);
        if (messageHolder!=null) {
            // 指定Channel
            messageHolder.setChannel(ctx.channel());
            // 添加到任务队列
            boolean offer = taskQueue.offer(messageHolder);
            logger.info("TaskQueue添加任务: taskQueue=" + taskQueue.size());
            if (!offer) {
                // 服务器繁忙
                logger.warn("服务器繁忙，拒绝服务");
                // 繁忙响应
                response(ctx.channel(), messageHolder.getSign());
            }
        } else {
            throw new IllegalArgumentException("msg is not instance of MessageHolder");
        }

    }
    //第一次请求是http请求，请求头包括ws的信息
    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){


        if(!req.decoderResult().isSuccess()){

            sendHttpResponse(ctx,req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws:/"+ctx.channel()+ "/websocket",null,false);
        handshaker = wsFactory.newHandshaker(req);


        if(handshaker == null){
            //不支持
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else{

            handshaker.handshake(ctx.channel(), req);
        }

    }

    public static void sendHttpResponse(ChannelHandlerContext ctx,FullHttpRequest req,DefaultFullHttpResponse res){


        // 返回应答给客户端
        if (res.status().code() != 200)
        {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }

        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.status().code() != 200)
        {
            f.addListener(ChannelFutureListener.CLOSE);
        }

    }

    private static boolean isKeepAlive(FullHttpRequest req)
    {
        return false;
    }

    /**
     * 服务器繁忙响应
     *
     * @param channel
     * @param sign
     */
    private void response(Channel channel, int sign) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(sign);
        messageHolder.setStatus(ProtocolHeader.SERVER_BUSY);
        messageHolder.setBody("");
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        channel.writeAndFlush(tws);
    }
}
