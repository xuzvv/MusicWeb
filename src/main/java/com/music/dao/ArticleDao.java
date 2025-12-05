package com.music.dao;

import com.music.bean.Article;
import com.music.util.DBUtil;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ArticleDao {

    // 查找文章
    public Article getArticleByMusicId(int musicId) {
        Article article = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 手记不需要显示秒

        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT * FROM articles WHERE music_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, musicId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                article = new Article();
                article.setId(rs.getInt("id"));
                article.setMusicId(rs.getInt("music_id"));
                article.setContent(rs.getString("content"));

                // ✨ 格式化时间 ✨
                Timestamp ts = rs.getTimestamp("update_time");
                article.setUpdateTime(ts != null ? sdf.format(ts) : "暂无时间");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return article;
    }

    // 保存或更新文章
    public void saveOrUpdate(int musicId, String content) {
        try (Connection conn = DBUtil.getConn()) {
            Article exist = getArticleByMusicId(musicId);
            String sql;
            PreparedStatement ps;

            if (exist == null) {
                sql = "INSERT INTO articles(music_id, content) VALUES(?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, musicId);
                ps.setString(2, content);
            } else {
                sql = "UPDATE articles SET content = ? WHERE music_id = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, content);
                ps.setInt(2, musicId);
            }
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}