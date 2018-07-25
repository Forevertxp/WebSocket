package com.olstudent.info;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.json.Serializer;
import com.olstudent.operation.GroupDao;
import com.olstudent.pojo.MyGroup;
import com.olstudent.protocol.MessageHolder;
import com.olstudent.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.Future;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * 查看所在讨论组信息服务.
 *
 * @author txp.
 */
public class InfoGroup {
    private static final Logger logger = Logger.getLogger(InfoGroup.class);

    private Channel channel;
    private String username;

    public InfoGroup(MyGroup myGroup, Channel channel) {
        this.channel = channel;
        username = myGroup.getUsername();
    }

    public void deal() {
        GroupDao groupDao = null;
        try {
            groupDao = new GroupDao();
            groupDao.connect();
            Map<String, List<String>> groups = groupDao.queryAllbyMember(username);
            MyGroup myGroup = new MyGroup();
            myGroup.setUsername(username);
            myGroup.setGroups(groups);
            String body = Serializer.serialize(myGroup);
            logger.info(username + " 查询讨论组信息");
            Future future = sendResponse(ProtocolHeader.SUCCESS, body);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        sendResponse(ProtocolHeader.SUCCESS, body);
                    }
                }
            });
        } finally {
            if (groupDao != null) {
                groupDao.close();
            }
        }
    }

    private Future sendResponse(int status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.LOOK_GROUP_INFO);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
