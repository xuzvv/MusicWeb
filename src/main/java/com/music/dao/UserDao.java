package com.music.dao;

import com.music.bean.User;
import com.music.util.DBUtil;
import java.sql.*;

public class UserDao {
    // 登录验证
    public User login(String username, String password) {
        User user = null;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = mapResultToUser(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return user;
    }

    // 注册
    public boolean register(String username, String password) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 【新增】根据用户名获取信息 (用于查看他人主页)
    public User getUserByUsername(String username) {
        User user = null;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT * FROM users WHERE username=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = mapResultToUser(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return user;
    }

    // 【新增】更新用户资料
    public void updateUser(User u) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "UPDATE users SET nickname=?, bio=?, social_link=?, avatar=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u.getNickname());
            ps.setString(2, u.getBio());
            ps.setString(3, u.getSocialLink());
            ps.setString(4, u.getAvatar());
            ps.setInt(5, u.getId());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 辅助方法：封装User对象
    private User mapResultToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setRole(rs.getString("role"));
        user.setNickname(rs.getString("nickname"));
        user.setBio(rs.getString("bio"));
        user.setSocialLink(rs.getString("social_link"));
        user.setAvatar(rs.getString("avatar"));
        // 如果头像为空，生成一个基于用户名的随机头像
        if(user.getAvatar() == null || user.getAvatar().isEmpty()){
            user.setAvatar("https://api.dicebear.com/7.x/identicon/svg?seed=" + user.getUsername());
        }
        return user;
    }
}