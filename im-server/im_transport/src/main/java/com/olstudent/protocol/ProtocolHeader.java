package com.olstudent.protocol;

/**
 * Jelly传输层协议头.
 *
 *                                       Jelly Protocol
 *  __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __
 * |           |           |           |           |              |                          |
 *       2           1           1           1            4               Uncertainty
 * |__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __ __|__ __ __ __ __ __ __ __ __|
 * |           |           |           |           |              |                          |
 *     Magic        Sign        Type       Status     Body Length         Body Content
 * |__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __ __|__ __ __ __ __ __ __ __ __|
 *
 * 协议头9个字节定长
 *     Magic      // 数据包的验证位，short类型
 *     Sign       // 消息标志，请求／响应／通知，byte类型
 *     Type       // 消息类型，登录／发送消息等，byte类型
 *     Status     // 响应状态，成功／失败，byte类型
 *     BodyLength // 协议体长度，int类型
 *
 *
 * @author txp.
 */
public class ProtocolHeader {

    /** 协议头长度 */
    public static final int HEADER_LENGTH = 9;
    /** Magic */
    public static final short MAGIC = (short) 0xabcd;

    /** 消息标志 */
    private byte sign;

    /** sign: 1 ~ 3 =========================================================================================== */
    public static final int REQUEST               = 1;    // 请求  Client --> Server
    public static final int RESPONSE              = 2;    // 响应  Server --> Client
    public static final int NOTICE                = 3;    // 通知  Server --> Client  e.g.消息转发

    /** 消息类型 */
    private byte type;

    /** type: 1 ~ 18 =========================================================================================== */
    public static final int LOGIN                 = 1;    // 登录
    public static final int REGISTER              = 2;    // 注册
    public static final int LOGOUT                = 3;    // 登出
    public static final int RECONN                = 4;    // 重连
    public static final int PERSON_MESSAGE        = 5;    // 发送个人消息
    public static final int GROUP_MESSAGE         = 6;    // 发送讨论组消息
    public static final int CREATE_GROUP          = 7;    // 创建讨论组
    public static final int DISBAND_GROUP         = 8;    // 解散讨论组
    public static final int ADD_MEMBER            = 9;    // 讨论组添加成员
    public static final int REMOVE_MEMBER         = 10;    // 讨论组移除成员
    public static final int ADD_FRIEND            = 11;    // 添加好友
    public static final int REMOVE_FRIEND         = 12;    // 删除好友
    public static final int ALL_FRIEND            = 13;    // 查看已添加好友
    public static final int UPDATE_SELF_INFO      = 14;    // 修改个人信息
    public static final int LOOK_SELF_INFO        = 15;    // 查看个人信息
    public static final int LOOK_FRIEND_INFO      = 16;    // 查看好友信息
    public static final int LOOK_GROUP_INFO       = 17;    // 查看自己所在讨论组信息
    public static final int HEARTBEAT             = 18;    // 心跳

    /** 响应状态 */
    private byte status;

    /** status: 0x31 ~ 0x34 ========================================================================================= */
    public static final int SUCCESS               = 49;    // 49 请求成功
    public static final int REQUEST_ERROR         = 50;    // 50 请求错误
    public static final int SERVER_BUSY           = 51;    // 51 服务器繁忙
    public static final int SERVER_ERROR          = 51;    // 52 服务器错误

    /** 消息体长度 */
    private int bodyLength;
}
