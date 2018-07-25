package com.olstudent.operation;

import com.olstudent.connect.JdbcConn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 讨论组数据访问接口
 * <p>
 * @author txp.
 */
public class GroupDao extends JdbcConn {

    /**
     * 添加一个讨论组
     *
     * @param groupName
     * @param creater
     * @return
     */
    public int insertGroup(String groupName, String creater) {
        String sql_1 = "INSERT INTO im_groups (name, creater) VALUES (?, ?)";
        String sql_2 = "INSERT INTO im_groups_user  (group_name, user_phone) VALUES (?, ?)";
        int row = 0;
        try {
            // 创建组
            pstmt = conn.prepareStatement(sql_1);
            pstmt.setString(1, groupName);
            pstmt.setString(2, creater);
            row = pstmt.executeUpdate();
            // 创建组与管理员的关联关系
            pstmt = conn.prepareStatement(sql_2);
            pstmt.setString(1, groupName);
            pstmt.setString(2, creater);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加讨论组出现异常", e);
        }
        return row;
    }

    /**
     * 删除讨论组
     *
     * @param groupName
     * @return
     */
    public int removeGroup(String groupName) {
        String sql = "DELETE FROM im_groups where name = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL删除讨论组信息出现异常", e);
        }
        return row;
    }

    /**
     * 添加成员
     *
     * @param groupName
     * @param member
     * @return
     */
    public int insertMember(String groupName, String member) {
        String sql = "INSERT INTO im_groups_user  (group_name, user_phone) VALUES (?, ?)";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setString(2, member);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加讨论组成员出现异常", e);
        }
        return row;
    }

    /**
     * 删除成员
     *
     * @param groupName
     * @param userName
     * @return
     */
    public int removeMember(String groupName, String userName) {
        String sql = "DELETE from im_groups_user where group_name = ? and user_phone = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setString(2, userName);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL删除讨论组成员出现异常", e);
        }
        return row;
    }

    /**
     * 查询用户是否已在群中
     *
     * @param groupName
     * @return 返回值为null，讨论组不存在
     */
    public int queryMember(String groupName,String member) {
        String sql = "SELECT * FROM im_groups_user WHERE group_name = ? and user_phone";
        int rowCount = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setString(1, member);
            resultSet = pstmt.executeQuery();
            while(resultSet.next()) {
                rowCount++;
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询讨论组成员出现异常", e);
        }
        return rowCount;
    }

    /**
     * 根据讨论组名称查询成员
     *
     * @param groupName
     * @return 返回值为null，讨论组不存在
     */
    public List<String> queryMemberByGroupName(String groupName) {
        List<String> members = null;
        String sql = "SELECT * FROM im_groups_user WHERE group_name = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            resultSet = pstmt.executeQuery();
            members = new ArrayList<String>();
            while (resultSet.next()) {
                members.add(resultSet.getString("user_phone"));
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询讨论组成员出现异常", e);
        }
        return members.size()>0?members:null;
    }

    /**
     * 查询全部存在member的讨论组以及成员
     *
     * @param member
     * @return
     */
    public Map<String, List<String>> queryAllbyMember(String member) {
        Map<String, List<String>> groups = new HashMap<String, List<String>>();
        String sql = "SELECT * FROM im_groups";
        try {
            pstmt = conn.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                boolean isExist = false;
                List<String> members = new ArrayList<String>();
                String creater = resultSet.getString("creater");
                members.add(creater);
                if (member.equals(creater)) {
                    isExist = true;
                }
                String groupName = resultSet.getString("name");
                List<String> memberList = queryMemberByGroupName(groupName);
                for (int i = 0; i < memberList.size(); i++) {
                    String m = memberList.get(i);
                    if (m != null) {
                        members.add(m);
                    }
                    if (member.equals(m)) {
                        isExist = true;
                    }
                }
                if (isExist) {
                    groups.put(resultSet.getString("name"), members);
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询全部讨论组出现异常", e);
        }
        return groups;
    }
}
