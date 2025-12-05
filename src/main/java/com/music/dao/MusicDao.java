package com.music.dao;

import com.music.bean.Music;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MusicDao {

    // 1. 通用分页查询 (首页用)
    public List<Music> getMusicList(String type, int page, int size) {
        List<Music> list = new ArrayList<>();
        int offset = (page - 1) * size;
        String orderBy = "m.play_count DESC";
        if ("new".equals(type)) {
            orderBy = "m.upload_time DESC";
        } else if ("random".equals(type)) {
            orderBy = "RAND()";
        }

        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.status=1 ORDER BY " + orderBy + " LIMIT ?, ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, offset);
            ps.setInt(2, size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultToMusic(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. 搜索音乐
    public List<Music> searchMusic(String keyword, int page, int size) {
        List<Music> list = new ArrayList<>();
        int offset = (page - 1) * size;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.status=1 AND " +
                    "(m.title LIKE ? OR m.artist LIKE ? OR m.uploader_name LIKE ? OR u.nickname LIKE ?) " +
                    "ORDER BY m.id DESC LIMIT ?, ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            String likeKey = "%" + keyword + "%";
            for(int i=1; i<=4; i++) ps.setString(i, likeKey);
            ps.setInt(5, offset);
            ps.setInt(6, size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultToMusic(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. 查询总数
    public int getMusicCount() {
        int count = 0;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT count(*) FROM music WHERE status=1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // 4. 搜索总数
    public int getSearchCount(String keyword) {
        int count = 0;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT count(*) FROM music m LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.status=1 AND (m.title LIKE ? OR m.artist LIKE ? OR m.uploader_name LIKE ? OR u.nickname LIKE ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String likeKey = "%" + keyword + "%";
            for(int i=1; i<=4; i++) ps.setString(i, likeKey);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // 5. 获取个人作品
    public List<Music> getMusicByUploader(String uploaderName) {
        List<Music> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.uploader_name = ? AND m.status = 1 ORDER BY m.id DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, uploaderName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultToMusic(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 6. 随机推荐
    public List<Music> getRandomMusicList(int size) {
        return getMusicList("random", 1, size);
    }

    // ✨✨✨ 7. 【核心修复】获取待审核音乐 (加入 LEFT JOIN) ✨✨✨
    public List<Music> getPendingMusic() {
        List<Music> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            // 旧代码: SELECT * FROM music WHERE status=0 (缺少 nickname 列)
            // 新代码: 加上 LEFT JOIN
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.status=0 ORDER BY m.upload_time DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultToMusic(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ✨✨✨ 8. 【核心修复】获取所有音乐 (加入 LEFT JOIN) ✨✨✨
    public List<Music> getAllMusic() {
        List<Music> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "ORDER BY m.id DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultToMusic(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 9. 辅助映射
    private Music mapResultToMusic(ResultSet rs) throws SQLException {
        Music m = new Music();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setArtist(rs.getString("artist"));
        m.setFilePath(rs.getString("file_path"));
        m.setPlayCount(rs.getInt("play_count"));
        m.setStatus(rs.getInt("status"));
        m.setUploaderName(rs.getString("uploader_name"));

        // 读取 nickname，如果读不到(null)则说明该行数据可能来自未Join的查询(已修复)或用户已被删
        // 这里的 try-catch 是为了防止万一哪里还有漏网之鱼，保证不崩
        try {
            String nick = rs.getString("nickname");
            m.setUploaderNickname((nick != null && !nick.isEmpty()) ? nick : m.getUploaderName());
        } catch (SQLException e) {
            m.setUploaderNickname(m.getUploaderName()); // 降级处理
        }

        String d = rs.getString("duration");
        m.setDuration(d == null ? "00:00" : d);
        try { m.setUploadTime(rs.getString("upload_time")); } catch (SQLException e) {}
        return m;
    }

    // --- 增删改 ---
    public void saveMusic(Music m) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO music(title, artist, file_path, status, uploader_name, duration) VALUES(?, ?, ?, 0, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getArtist());
            ps.setString(3, m.getFilePath());
            ps.setString(4, m.getUploaderName());
            ps.setString(5, m.getDuration() == null ? "00:00" : m.getDuration());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void addPlayCount(int id) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "UPDATE music SET play_count = play_count + 1 WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public Music getMusicById(int id) {
        Music m = null;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m LEFT JOIN users u ON m.uploader_name = u.username WHERE m.id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){ m = mapResultToMusic(rs); }
        } catch (Exception e) { e.printStackTrace(); }
        return m;
    }
    public void approveMusic(int id) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "UPDATE music SET status = 1 WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void deleteMusic(int id) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "DELETE FROM music WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public List<Music> getHotMusic() { return getMusicList("hot", 1, 20); }
}