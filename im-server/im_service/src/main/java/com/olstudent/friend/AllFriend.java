package com.olstudent.friend;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.connection.ConnPool;
import com.olstudent.json.Serializer;
import com.olstudent.operation.FriendDao;
import com.olstudent.pojo.MyFriend;
import com.olstudent.protocol.MessageHolder;
import com.olstudent.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.Future;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询已添加好友服务.
 *
 * @author txp.
 */
public class AllFriend {
    private static final Logger logger = Logger.getLogger(AllFriend.class);

    private Channel channel;
    private String username;
    private MyFriend myFriend;

    public AllFriend(MyFriend myFriend, Channel channel) {
        this.myFriend = myFriend;
        this.channel = channel;
        username = myFriend.getUsername();
    }

    public void deal() {
        FriendDao friendDao = null;
        try {
            friendDao = new FriendDao();
            friendDao.connect();
            List<String> friends = friendDao.queryAllFri(username);
            Map<String, Boolean> friMap = new HashMap<>();
            if (friends.size() != 0) {
                for (int i = 0; i < friends.size(); i++) {
                    String friend = friends.get(i);
                    // 查询好友是否在线
                    if (ConnPool.query(friend) != null) {
                        // 在线
                        friMap.put(friend, true);
                    } else {
                        // 离线
                        friMap.put(friend, false);
                    }
                }

            }
            myFriend.setFriends(friMap);
            logger.info(username + " 查询好友");

            Future future = sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(myFriend));
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(myFriend));
                    }
                }
            });

        } finally {
            if (friendDao != null) {
                friendDao.close();
            }
        }
    }

    private Future sendResponse(int status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.ALL_FRIEND);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
