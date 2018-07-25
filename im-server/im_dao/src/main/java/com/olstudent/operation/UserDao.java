package com.olstudent.operation;

import com.olstudent.connect.JdbcConn;
import com.olstudent.pojo.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问类
 * <p>
 * @author txp.
 */
public class UserDao extends JdbcConn {

    /**
     * 添加用户
     *
     * @param phone
     * @param password
     * @return
     */
    public int insertUser(String phone, String password) {
        String sql = "INSERT INTO im_user (phone, passwd) VALUES (?, ?)";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            pstmt.setString(2, password);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加用户出现异常", e);
        }
        return row;
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param phone
     * @return
     */
    public List<User> queryByPhone(String phone) {
        List<User> users = new ArrayList<User>();
        String sql = "SELECT * FROM im_user WHERE phone = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("phone"));
                user.setPassword(resultSet.getString("passwd"));
                user.setName(resultSet.getString("name"));
                user.setSex(resultSet.getString("sex"));
                user.setAge(resultSet.getString("age"));
                user.setPhone(resultSet.getString("realName"));
                user.setAddress(resultSet.getString("school"));
                user.setIntroduction(resultSet.getString("introduction"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询用户出现异常", e);
        }
        return users;
    }

    /**
     * 修改姓名
     *
     * @param phone
     * @param name
     * @return
     */
    public int updateName(String phone, String name) {
        String sql = "update im_user set name = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL修改姓名出现异常", e);
        }
        return row;
    }

    /**
     * 修改密码
     *
     * @param phone
     * @param password
     * @return
     */
    public int updatePassword(String phone, String password) {
        String sql = "update im_user set password = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, password);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL修改密码出现异常", e);
        }
        return row;
    }

    /**
     * 修改性别
     *
     * @param phone
     * @param sex
     * @return
     */
    public int updateSex(String phone, String sex) {
        String sql = "update im_user set sex = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sex);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL修改性别出现异常", e);
        }
        return row;
    }

    /**
     * 修改年龄
     *
     * @param phone
     * @param age
     * @return
     */
    public int updateAge(String phone, String age) {
        String sql = "update im_user set age = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, age);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL修改年龄出现异常", e);
        }
        return row;
    }

    /**
     * 修改地址
     *
     * @param phone
     * @param address
     * @return
     */
    public int updateAddress(String phone, String address) {
        String sql = "update im_user set address = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, address);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL修改地址出现异常", e);
        }
        return row;
    }

    /**
     * 修改自我介绍
     *
     * @param phone
     * @param introduction
     * @return
     */
    public int updateIntroduction(String phone, String introduction) {
        String sql = "update im_user set introduction = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, introduction);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL修改自我介绍出现异常", e);
        }
        return row;
    }
}
