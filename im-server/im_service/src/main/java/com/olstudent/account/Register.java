package com.olstudent.account;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.json.Serializer;
import com.olstudent.operation.FriendDao;
import com.olstudent.operation.UserDao;
import com.olstudent.pojo.Account;
import com.olstudent.pojo.User;
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
 * 注册服务.
 *
 * @author txp.
 */
public class Register {
    private static final Logger logger = Logger.getLogger(Register.class);

    private String phone;
    private String password;
    private Channel channel;

    public Register(Account account, Channel channel) {
        phone = account.getPhone();
        password = account.getPassword();
        this.channel = channel;
    }

    public void deal() {
        UserDao userDao = null;
        FriendDao friendDao = null;
        // 查询用户名是否已存在
        try {
            userDao = new UserDao();
            friendDao = new FriendDao();
            userDao.connect();
            friendDao.connect();
            List<User> users = userDao.queryByPhone(phone);
            if (users.size() == 0) {
                // 添加用户
                int r1 = userDao.insertUser(phone, MD5.md5(password));
                int r2 = friendDao.insertAccount(phone);
                if (r1 == 1 && r2 == 1) {
                    // 成功
                    success();
                    logger.info(phone + " 注册成功");
                } else {
                    // 失败，数据库错误
                    defeat(ProtocolHeader.SERVER_ERROR);
                    logger.warn("注册时数据库出现错误");
                }
            } else {
                // 失败，用户名已存在
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("注册失败(用户名已存在)");
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
            if (friendDao != null) {
                friendDao.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void success() {
        // 返回用户名
        Account account = new Account();
        account.setPhone(phone);
        Future future = sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(account));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 关闭连接
                    channel.close().sync();
                } else {
                    sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(account))
                            .addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (future.isSuccess()) {
                                        // 关闭连接
                                        channel.close().sync();
                                    }
                                }
                            });
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void defeat(int status) {
        Future future = sendResponse(status, "");
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 关闭连接
                    channel.close().sync();
                } else {
                    sendResponse(status, "").addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                // 关闭连接
                                channel.close().sync();
                            }
                        }
                    });
                }
            }
        });
    }

    private Future sendResponse(int status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.REGISTER);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
