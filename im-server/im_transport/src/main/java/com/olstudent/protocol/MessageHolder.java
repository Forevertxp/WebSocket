package com.olstudent.protocol;

import io.netty.channel.Channel;

/**
 * 消息载体.
 *
 * 传输模块与服务模块之间双向数据传输载体:
 *
 *                   MessageHolder
 * Service Module <----------------> Transport Module
 *
 * @author txp.
 */
public class MessageHolder {

    // 消息标志
    private int sign;
    // 消息类型
    private int type;
    // 响应状态
    private int status;
    // Json消息体
    private String body;
    // 接收到消息的通道
    private Channel channel;

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "MessageHolder{" +
                "sign=" + sign +
                ", type=" + type +
                ", status=" + status +
                ", body='" + body + '\'' +
                ", channel=" + channel +
                '}';
    }
}
