package com.olstudent.info;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.json.Serializer;
import com.olstudent.operation.FriendDao;
import com.olstudent.operation.UserDao;
import com.olstudent.pojo.Friend;
import com.olstudent.pojo.Info;
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
 * 查看好友个人信息服务.
 *
 * @author txp.
 */
public class InfoFriend {
    private static final Logger logger = Logger.getLogger(InfoFriend.class);

    private Channel channel;
    private Friend friend;

    public InfoFriend(Friend friend, Channel channel) {
        this.channel = channel;
        this.friend = friend;
    }

    public void deal() {
        // 查询是否为好友
        UserDao userDao = null;
        FriendDao friendDao = null;
        try {
            userDao = new UserDao();
            friendDao = new FriendDao();
            userDao.connect();
            friendDao.connect();
            String c = friendDao.queryColumnByFri(friend.getPhone(), friend.getFriend());
            if (c != null) {
                // 查询信息
                List<User> users = userDao.queryByPhone(friend.getFriend());
                if (users.size() == 1) {
                    User user = users.get(0);
                    success(user);
                    logger.info(friend.getPhone() + "查询好友信息(" + friend.getFriend() + ") 成功");
                } else {
                    // 数据库异常
                    defeat(ProtocolHeader.SERVER_ERROR);
                    logger.warn("查询好友信息 数据库异常");
                }
            } else {
                // 未添加好友
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("查询好友信息 失败 不是好友");
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
    private void success(User user) {
        Info info = new Info();
        info.setUsername(user.getUsername());
        info.setName(user.getName());
        info.setSex(user.getSex());
        info.setAge(user.getAge());
        info.setPhone(user.getPhone());
        info.setAddress(user.getAddress());
        info.setIntroduction(user.getIntroduction());
        String body = Serializer.serialize(info);
        Future future = sendResponse(ProtocolHeader.SUCCESS, body);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    sendResponse(ProtocolHeader.SUCCESS, body);
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
                    sendResponse(status, "");
                }
            }
        });
    }

    private Future sendResponse(int status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.LOOK_FRIEND_INFO);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
