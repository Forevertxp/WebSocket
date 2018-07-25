package com.olstudent.operation;

import com.olstudent.connect.JdbcConn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 好友数据访问类
 * <p>
 * @author txp.
 */
public class FriendDao extends JdbcConn {

    /**
     * 在好友表中添加账户
     *
     * @param phone
     * @return
     */
     public int insertAccount(String phone) {
        String sql = "INSERT INTO im_friends (phone) VALUES (?)";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加账户出现异常", e);
        }
        return row;
    }

    /**
     * 添加好友
     *
     * @param phone
     * @param friend
     * @param column
     * @return
     */
    public int insertFriend(String phone, String friend, String column) {
        String sql = "update im_friends set " + column + " = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, friend);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加好友出现异常", e);
        }
        return row;
    }

    /**
     * 删除好友
     *
     * @param phone
     * @return
     */
    public int removeFriend(String phone, String column) {
        String sql = "update im_friends set " + column + " = ? where phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, null);
            pstmt.setString(2, phone);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL删除好友出现异常", e);
        }
        return row;
    }

    /**
     * 查询所有好友
     *
     * @param phone
     * @return
     */
    public List<String> queryAllFri(String phone) {
        List<String> friends = new ArrayList<String>();
        String sql = "SELECT * FROM im_friends WHERE phone = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 20; i++) {
                    String friend = resultSet.getString("friend_" + i);
                    if (friend != null) {
                        friends.add(friend);
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询所有好友出现异常", e);
        }
        return friends;
    }

    /**
     * 查询好友空列
     *
     * @param phone
     * @return 列名
     */
    public String queryNoFriColumn(String phone) {
        String sql = "SELECT * FROM im_friends WHERE phone = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 20; i++) {
                    if (resultSet.getString("friend_" + i) == null) {
                        return "friend_" + i;
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询好友空位出现异常", e);
        }
        return null;
    }

    /**
     * 查询指定好友所在列名
     *
     * @param phone
     * @param friend
     * @return
     */
    public String queryColumnByFri(String phone, String friend) {
        String sql = "SELECT * FROM im_friends WHERE phone = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 20; i++) {
                    if (friend.equals(resultSet.getString("friend_" + i))) {
                        return "friend_" + i;
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询好友所在列名出现异常", e);
        }
        return null;
    }
}
