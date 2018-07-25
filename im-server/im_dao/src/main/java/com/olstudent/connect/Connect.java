package com.olstudent.connect;

/**
 * The interface of Database;
 *
 * @author txp.
 */
public interface Connect {

    /**
     * 连接MySQL数据库
     */
    void connect();

    /**
     * 关闭数据库资源
     */
    void close();
}
