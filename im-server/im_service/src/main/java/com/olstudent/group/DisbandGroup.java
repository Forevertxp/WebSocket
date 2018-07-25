package com.olstudent.group;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.operation.GroupDao;
import com.olstudent.pojo.Group;
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
 * 解散讨论组服务.
 *
 * @author txp.
 */
public class DisbandGroup {
    private static final Logger logger = Logger.getLogger(DisbandGroup.class);

    private Channel channel;
    private String phone;
    private String groupName;

    public DisbandGroup(Group cGroup, Channel channel) {
        this.channel = channel;
        phone = cGroup.getPhone();
        groupName = cGroup.getGroupName();
    }

    public void deal() {
        // 查询讨论组是否存在
        GroupDao groupDao = null;
        try {
            groupDao = new GroupDao();
            groupDao.connect();
            List<String> members = groupDao.queryMemberByGroupName(groupName);
            if (members != null) {
                if (members.get(0).equals(phone)) {
                    int r = groupDao.removeGroup(groupName);
                    if (r == 1) {
                        success();
                        logger.info(phone + " 解散讨论组 " + groupName + " 成功");
                    } else {
                        defeat(ProtocolHeader.SERVER_ERROR);
                        logger.warn("数据库错误");
                    }
                } else {
                    // 非创建者操作
                    defeat(ProtocolHeader.REQUEST_ERROR);
                    logger.info(phone + " 解散讨论组 " + groupName + " 失败(非讨论创建者不能执行解散操作)");
                }
            } else {
                // 讨论组不存在
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info(phone + " 解散讨论组 " + groupName + " 失败（讨论组不存在）");
            }
        } finally {
            if (groupDao != null) {
                groupDao.close();
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
        messageHolder.setType(ProtocolHeader.DISBAND_GROUP);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
