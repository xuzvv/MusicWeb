package com.music.dao;

import com.music.bean.Comment;
import com.music.util.DBUtil;
import java.sql.*;
import java.text.SimpleDateFormat;
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

    // 获取评论列表
    public List<Comment> getCommentsByMusicId(int musicId) {
        List<Comment> list = new ArrayList<>();
        // ✨ 定义时间格式 ✨
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = DBUtil.getConn()) {
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

                // ✨ 使用 getTimestamp + SimpleDateFormat 格式化 ✨
                Timestamp ts = rs.getTimestamp("create_time");
                c.setCreateTime(ts != null ? sdf.format(ts) : "");

                c.setNickname(rs.getString("nickname"));
                list.add(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}