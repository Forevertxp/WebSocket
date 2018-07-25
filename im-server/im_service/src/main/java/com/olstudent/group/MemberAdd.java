package com.olstudent.group;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.operation.GroupDao;
import com.olstudent.operation.UserDao;
import com.olstudent.pojo.Member;
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
 * 讨论组添加成员服务.
 *
 * @author txp.
 */
public class MemberAdd {
    private static final Logger logger = Logger.getLogger(MemberAdd.class);

    private Channel channel;
    private String phone;
    private String member;
    private String groupName;

    public MemberAdd(Member aMember, Channel channel) {
        this.channel = channel;
        phone = aMember.getPhone();
        member = aMember.getMember();
        groupName = aMember.getGroupName();
    }

    public void deal() {
        UserDao userDao = null;
        GroupDao groupDao = null;
        try {
            userDao = new UserDao();
            groupDao = new GroupDao();
            userDao.connect();
            groupDao.connect();
            // 讨论组是否存在
            List<String> members = groupDao.queryMemberByGroupName(groupName);
            if (members != null) {
                // 用户是否存在
                List<User> users = userDao.queryByPhone(member);
                int isExist = groupDao.queryMember(groupName,member);
                if (users.size() == 1) {
                    if (isExist>0){
                        defeat(ProtocolHeader.REQUEST_ERROR);
                        logger.info("邀请进入讨论组<" + groupName + "> " + phone + "-->" + member + " 失败（已在群组中）");
                    }else{
                        // 邀请者是否为创建者
                        if (members.get(0).equals(phone)) {
                            // 添加成员
                            int r = groupDao.insertMember(groupName, member);
                            if (r == 1) {
                                success();
                                logger.info("邀请进入讨论组<" + groupName + "> " + phone + "-->" + member + " 成功");
                            }
                        } else {
                            defeat(ProtocolHeader.REQUEST_ERROR);
                            logger.info("邀请进入讨论组<" + groupName + "> " + phone + "-->" + member + " 失败（非讨论组创建者不能邀请成员）");
                        }
                    }

                } else {
                    defeat(ProtocolHeader.REQUEST_ERROR);
                    logger.info("邀请进入讨论组<" + groupName + "> " + phone + "-->" + member + " 失败（用户不存在）");
                }
            } else {
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("邀请进入讨论组<" + groupName + "> " + phone + "-->" + member + " 失败（讨论组不存在）");
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
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
        messageHolder.setType(ProtocolHeader.ADD_MEMBER);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
