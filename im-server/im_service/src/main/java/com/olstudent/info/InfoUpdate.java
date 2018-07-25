package com.olstudent.info;

import com.alibaba.fastjson.JSONObject;
import com.olstudent.operation.UserDao;
import com.olstudent.pojo.Info;
import com.olstudent.protocol.MessageHolder;
import com.olstudent.protocol.ProtocolHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.Future;
import org.apache.log4j.Logger;

/**
 * 修改个人信息服务.
 *
 * @author txp.
 */
public class InfoUpdate {
    private static final Logger logger = Logger.getLogger(InfoUpdate.class);

    private Channel channel;

    private String username;
    private String password;
    private String name;
    private String sex;
    private String age;
    private String phone;
    private String address;
    private String introduction;

    public InfoUpdate(Info uInfo, Channel channel) {
        this.channel = channel;
        username = uInfo.getUsername();
        password = uInfo.getPassword();
        name = uInfo.getName();
        sex = uInfo.getSex();
        age = uInfo.getAge();
        phone = uInfo.getPhone();
        address = uInfo.getAddress();
        introduction = uInfo.getIntroduction();
    }

    public void deal() {
        int r1 = 1, r2 = 1, r3 = 1, r4 = 1, r5 = 1, r6 = 1;

        UserDao userDao = null;

        try {
            userDao = new UserDao();
            userDao.connect();

            if (password != null) {
                // 修改密码
                r1 = userDao.updatePassword(username, password);
            }
            if (name != null) {
                // 修改姓名
                r2 = userDao.updateName(username, name);
            }
            if (sex != null) {
                r3 = userDao.updateSex(username, sex);
            }
            if (age != null) {
                r4 = userDao.updateAge(username, age);
            }
            if (address != null) {
                r5 = userDao.updateAddress(username, address);
            }
            if (introduction != null) {
                r6 = userDao.updateIntroduction(username, introduction);
            }

            // 发送响应
            if (r1 + r2 + r3 + r4 + r5 + r6 == 6) {
                logger.info(username + " 修改信息 成功");
                success();
            } else {
                logger.warn(username + " 修改信息 失败（数据库错误）");
                defeat(ProtocolHeader.SERVER_ERROR);
            }

        } finally {
            if (userDao != null) {
                userDao.close();
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
        messageHolder.setType(ProtocolHeader.UPDATE_SELF_INFO);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        String jsonString = JSONObject.toJSONString(messageHolder);
        TextWebSocketFrame tws = new TextWebSocketFrame(jsonString);
        return channel.writeAndFlush(tws);
    }
}
