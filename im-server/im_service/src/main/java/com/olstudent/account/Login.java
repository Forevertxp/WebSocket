package com.olstudent.account;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.connection.ConnPool;
import com.olstudent.connection.TokenFactory;
import com.olstudent.connection.TokenPool;
import com.olstudent.handler.HeartbeatHandler;
import com.olstudent.json.Serializer;
import com.olstudent.operation.GroupMsgDao;
import com.olstudent.operation.MsgDao;
import com.olstudent.operation.UserDao;
import com.olstudent.pojo.*;
import com.olstudent.protocol.MessageHolder;
import com.olstudent.protocol.ProtocolHeader;
import com.olstudent.utils.MD5;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.Future;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 登录服务.
 * <p>
 *
 * @author txp.
 */
public class Login {
    private static final Logger logger = Logger.getLogger(Login.class);

     private Channel channel;
     private String phone;
     private String password;

    public Login(Account account, Channel channel) {
        phone = account.getPhone();
        password = account.getPassword();
        this.channel = channel;
    }

     /**
     * 登录信息验证
     */
    public void deal() {
        UserDao userDao = null;
        try {
            userDao = new UserDao();
            userDao.connect();
            List<User> users = userDao.queryByPhone(phone);
            if (users.size() == 1) {
                if (MD5.md5(password).equals(users.get(0).getPassword())) {
                    // 成功
                    success();
                } else {
                    // 失败，密码错误
                    defeat(ProtocolHeader.REQUEST_ERROR);
                }
            } else {
                // 失败，用户名错误
                defeat(ProtocolHeader.REQUEST_ERROR);
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
        }
    }

    /**
     * 信息验证成功
     */
    @SuppressWarnings("unchecked")
    private void success() {
        Long token = init();
        // 发送响应数据包
        Account acc = new Account();
        acc.setPhone(phone);
        acc.setToken(token);
        Future future = sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(acc));
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(phone + " 登录成功");
                    // 开启心跳检测
                    logger.info(phone + " 开启心跳检测");
                    channel.pipeline().addAfter("IdleStateHandler",
                            "HeartbeatHandler", new HeartbeatHandler(channel));

                    // 发送离线消息
                    sendOfflineMessage();

                } else {
                    sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(acc))
                            .addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (future.isSuccess()) {
                                        logger.info(phone + " 登录成功");
                                        // 开启心跳检测
                                        logger.info(phone + " 开启心跳检测");
                                        channel.pipeline().addAfter("IdleStateHandler",
                                                "HeartbeatHandler", new HeartbeatHandler(channel));

                                        // 发送离线消息
                                        sendOfflineMessage();
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * 信息验证失败
     *
     * @param status
     */
    @SuppressWarnings("unchecked")
    private void defeat(int status) {
        // 发送响应数据包
        Future future = sendResponse(status, "");
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(phone + " 登录失败");
                    channel.close().sync();
                } else {
                    sendResponse(status, "").addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                logger.info(phone + " 登录失败");
                                channel.close().sync();
                            }
                        }
                    });
                }

            }
        });
    }

    /**
     * 登录信息验证成功后的初始化
     *
     * @return
     */
    private Long init() {
        // 生成token
        TokenFactory factory = new TokenFactory();
        Long token = factory.generate();
        // 维护连接
        ConnPool.add(phone, channel);
        // 维护token
        TokenPool.add(token);
        return token;
    }

    private Future sendResponse(int status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.LOGIN);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }

    private void sendOfflineMessage() {
        // 个人消息
        personMessage();
        // 讨论组消息
        groupMessage();
    }

    /**
     * 发送个人离线消息
     */
    private void personMessage() {
        MsgDao msgDao = null;
        try {
            msgDao = new MsgDao();
            msgDao.connect();
            // 查询消息
            List<OfflineMessage> offlineMsgs = msgDao.queryMsg(phone);
            if (offlineMsgs.size() != 0) {
                // 一个一个发送
                for (int i = 0; i < offlineMsgs.size(); i++) {
                    OfflineMessage offlineMessage = offlineMsgs.get(i);
                    Message message = new Message();
                    message.setSender(offlineMessage.getSender());
                    message.setReceiver(offlineMessage.getReceiver());
                    message.setContent(offlineMessage.getMessage());
                    message.setTime(offlineMessage.getTime());
                    sendMessage(ProtocolHeader.PERSON_MESSAGE, channel, Serializer.serialize(message));
                    logger.info("个人消息(离线) " + message.getSender()
                            + "-->" + message.getReceiver() + " 发送成功");
                }
                // 删除离线消息
                int row = msgDao.removeMsg(phone);
                if (row == offlineMsgs.size()) {
                } else {
                    logger.warn("数据库错误");
                }
            } else {
                return;
            }
        } finally {
            if (msgDao != null) {
                msgDao.close();
            }
        }
    }

    /**
     * 发送讨论组离线消息
     */
    private void groupMessage() {
        GroupMsgDao groupMsgDao = null;
        try {
            groupMsgDao = new GroupMsgDao();
            groupMsgDao.connect();
            // 查询消息
            List<OfflineGroupMessage> offlineMsgs = groupMsgDao.queryMsg(phone);
            if (offlineMsgs.size() != 0) {
                // 一个一个发送
                for (int i = 0; i < offlineMsgs.size(); i++) {
                    OfflineGroupMessage offlineGroupMessage = offlineMsgs.get(i);
                    Message message = new Message();
                    message.setSender(offlineGroupMessage.getSender());
                    message.setReceiver(offlineGroupMessage.getGroup());
                    message.setContent(offlineGroupMessage.getMessage());
                    message.setTime(offlineGroupMessage.getTime());
                    sendMessage(ProtocolHeader.GROUP_MESSAGE, channel, Serializer.serialize(message));
                    logger.info("讨论组离线消息 " + offlineGroupMessage.getSender()
                            + "-->" + offlineGroupMessage.getGroup() + "-->" + phone + " 发送成功");
                }
                // 删除离线消息
                int row = groupMsgDao.removeMsg(phone);
                if (row == offlineMsgs.size()) {
                    logger.info("删除讨论组离线消息 成功");
                } else {
                    logger.warn("删除讨论组离线消息 失败");
                }
            } else {
                return;
            }
        } finally {
            if (groupMsgDao != null) {
                groupMsgDao.close();
            }
        }
    }

    /**
     * 发送消息
     *
     * @param type
     * @param recChannel
     * @param body
     * @return
     */
    private Future sendMessage(int type, Channel recChannel, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.NOTICE);
        messageHolder.setType(type);
        messageHolder.setStatus((byte) 0);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return recChannel.writeAndFlush(tws);
    }
}
