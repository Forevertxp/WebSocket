package com.olstudent.group;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.operation.GroupDao;
import com.olstudent.pojo.Member;
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
 * 讨论组删除成员服务.
 *
 * @author txp.
 */
public class MemberRemove {
    private static final Logger logger = Logger.getLogger(MemberRemove.class);

    private Channel channel;
    private String phone;
    private String member;
    private String groupName;

    public MemberRemove(Member aMember, Channel channel) {
        this.channel = channel;
        phone = aMember.getPhone();
        member = aMember.getMember();
        groupName = aMember.getGroupName();
    }

    public void deal() {
        GroupDao groupDao = null;
        try {
            groupDao = new GroupDao();
            groupDao.connect();
            // 讨论组是否存在
            List<String> members = groupDao.queryMemberByGroupName(groupName);
            if (members != null) {
                // 成员是否存在
                if (members.contains(member)) {
                    // 邀请者是否为创建者
                    if (members.get(0).equals(phone)) {
                        // 删除成员
                        int r = groupDao.removeMember(groupName, member);
                        if (r == 1) {
                            success();
                            logger.info("讨论组<" + groupName + "> " + "踢出成员 " + member + " 成功");
                        }
                    } else {
                        defeat(ProtocolHeader.REQUEST_ERROR);
                        logger.info("讨论组<" + groupName + "> " + "踢出成员 " + member + " 失败（非讨论组创建者不能踢出成员）");
                    }
                } else {
                    defeat(ProtocolHeader.REQUEST_ERROR);
                    logger.info("讨论组<" + groupName + "> " + "踢出成员 " + member + " 失败（成员不存在）");
                }
            } else {
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("讨论组<" + groupName + "> " + "踢出成员 " + member + " 失败（讨论组不存在）");
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
        messageHolder.setType(ProtocolHeader.REMOVE_MEMBER);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
