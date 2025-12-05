package com.music.dao;

import com.music.bean.Comment;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDao {
    // 添加评论
    public void addComment(Comment c) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO comments(music_id, username, content) VALUES(?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, c.getMusicId());
            ps.setString(2, c.getUsername());
            ps.setString(3, c.getContent());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ✨✨✨ 修改：获取评论列表 (联表查询用户昵称) ✨✨✨
    public List<Comment> getCommentsByMusicId(int musicId) {
        List<Comment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            // 关键 SQL：关联 users 表，获取 nickname
            String sql = "SELECT c.*, u.nickname FROM comments c " +
                    "LEFT JOIN users u ON c.username = u.username " +
                    "WHERE c.music_id = ? ORDER BY c.create_time DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, musicId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Comment c = new Comment();
                c.setId(rs.getInt("id"));
                c.setMusicId(rs.getInt("music_id"));
                c.setUsername(rs.getString("username"));
                c.setContent(rs.getString("content"));
                // 截取时间字符串
                String time = rs.getString("create_time");
                c.setCreateTime(time != null && time.length() > 19 ? time.substring(0, 19) : time);

                // ✨ 注入查询到的昵称
                c.setNickname(rs.getString("nickname"));

                list.add(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}