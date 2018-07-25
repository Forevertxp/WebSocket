package com.olstudent.friend;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.operation.FriendDao;
import com.olstudent.operation.UserDao;
import com.olstudent.pojo.Friend;
import com.olstudent.pojo.User;
import com.olstudent.protocol.MessageHolder;
import com.olstudent.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.Future;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 添加好友服务.
 *
 * @author txp.
 */
public class FriendAdd {
    private static final Logger logger = Logger.getLogger(FriendAdd.class);

    private Channel channel;
    private String phone;
    private String friend;

    public FriendAdd(Friend aFriend, Channel channel) {
        this.channel = channel;
        phone = aFriend.getPhone();
        friend = aFriend.getFriend();
    }

    public void deal() {
        UserDao userDao = null;
        FriendDao friendDao = null;
        try {
            userDao = new UserDao();
            friendDao = new FriendDao();
            userDao.connect();
            friendDao.connect();

            // 查询是否存在该用户
            List<User> users = userDao.queryByPhone(phone);
            if (users.size() == 1) {
                // 查询sender的好友数量是否达到上线
                String column = friendDao.queryNoFriColumn(phone);
                if (column != null) {
                    // 添加到好友列表
                    int row = friendDao.insertFriend(phone, friend, column);
                    if (row > 0) {
                        success();
                        logger.warn("添加好友 " + phone + "-->" + friend + " 添加成功");
                    } else {
                        defeat(ProtocolHeader.SERVER_ERROR);
                        logger.warn("添加好友 " + phone + "-->" + friend + " 添加失败(数据库错误)");
                    }
                } else {
                    // 好友数量爆满
                    defeat(ProtocolHeader.REQUEST_ERROR);
                    logger.info("添加好友 " + phone + "-->" + friend + " 添加失败（好友数量爆满）");
                }
            } else {
                // 不存在该用户
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("添加好友 " + phone + "-->" + friend + " 添加失败（不存在该用户）");
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
        Future future = sendResponse(ProtocolHeader.SUCCESS, "");
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    sendResponse(ProtocolHeader.SUCCESS, "");
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
                if (!future.isSuccess()) {
                    sendResponse(ProtocolHeader.SUCCESS, "");
                }
            }
        });
    }

    private Future sendResponse(int status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.ADD_FRIEND);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
