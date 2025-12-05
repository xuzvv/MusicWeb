package com.music.dao;

import com.music.bean.Danmaku;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanmakuDao {

    // 保存弹幕
    public void save(Danmaku d) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO danmaku(music_id, content, video_time) VALUES(?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, d.getMusicId());
            ps.setString(2, d.getContent());
            ps.setDouble(3, d.getVideoTime());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 获取某首歌的所有弹幕
    public List<Danmaku> getByMusicId(int musicId) {
        List<Danmaku> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT * FROM danmaku WHERE music_id = ? ORDER BY video_time ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, musicId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Danmaku d = new Danmaku();
                d.setContent(rs.getString("content"));
                d.setVideoTime(rs.getDouble("video_time"));
                list.add(d);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}