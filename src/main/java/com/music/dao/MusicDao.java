package com.music.dao;

import com.music.bean.Music;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.*;

public class MusicDao {

    // ================== 基础查询功能 (保持原有功能不变) ==================

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

    public List<Music> getRandomMusicList(int size) {
        return getMusicList("random", 1, size);
    }

    public List<Music> getPendingMusic() {
        List<Music> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.status=0 ORDER BY m.upload_time DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultToMusic(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

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

    // ================== ✨ 核心推荐算法 (完全重写) ==================

    // 9. 获取首页推荐列表 (逻辑：Top5 喜爱 -> Top4 点击 -> 剩余)
    public List<Music> getRecommendationForUser(int userId) {
        List<Music> allMusic = fetchAllMusicWithPreference(userId);
        if (allMusic.isEmpty()) return allMusic;

        // 1. 提取 Top 5 总喜爱度 (Total Preference)
        List<Music> byPref = new ArrayList<>(allMusic);
        byPref.sort((a, b) -> Double.compare(b.getTotalPreferenceSum(), a.getTotalPreferenceSum()));

        List<Music> topPrefList = new ArrayList<>();
        Set<Integer> usedIds = new HashSet<>();
        for (int i = 0; i < Math.min(5, byPref.size()); i++) {
            Music m = byPref.get(i);
            m.setRecommendType("red"); // UI显示火热
            topPrefList.add(m);
            usedIds.add(m.getId());
        }

        // 2. 提取 Top 4 点击次数 (Selection Count)，需排除已被选入 Top 5 的
        List<Music> byClick = new ArrayList<>(allMusic);
        byClick.sort((a, b) -> Integer.compare(b.getSelectionCount(), a.getSelectionCount()));

        List<Music> topClickList = new ArrayList<>();
        int count = 0;
        for (Music m : byClick) {
            if (count >= 4) break;
            if (!usedIds.contains(m.getId())) {
                m.setRecommendType("green"); // UI显示点击多
                topClickList.add(m);
                usedIds.add(m.getId());
                count++;
            }
        }

        // 3. 剩余歌曲 (按全站喜爱度排序)
        List<Music> restList = new ArrayList<>();
        for (Music m : byPref) { // 再次遍历按喜爱度排序的列表
            if (!usedIds.contains(m.getId())) {
                m.setRecommendType("normal");
                restList.add(m);
            }
        }

        // 4. 合并 (5 + 4 + Rest)
        List<Music> finalOrder = new ArrayList<>();
        finalOrder.addAll(topPrefList);
        finalOrder.addAll(topClickList);
        finalOrder.addAll(restList);

        return finalOrder;
    }

    // 10. 获取播放页推荐列表 (逻辑：序列预测 A->B 优先，不够再补首页逻辑)
    public List<Music> getRecommendationForPlayer(int userId, int currentMusicId) {
        List<Music> result = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();
        addedIds.add(currentMusicId); // 排除自己

        // 1. 查序列预测表 (Music Sequence Habits)
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname, seq.occurrence_count " +
                    "FROM music_sequence_habits seq " +
                    "JOIN music m ON seq.next_music_id = m.id " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE seq.current_music_id = ? AND m.status = 1 " +
                    "ORDER BY seq.occurrence_count DESC LIMIT 10";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentMusicId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Music m = mapResultToMusic(rs);
                m.setRecommendType("sequence"); // 标记为序列推荐
                result.add(m);
                addedIds.add(m.getId());
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. 如果不足 10 首，用首页推荐逻辑补齐
        if (result.size() < 10) {
            List<Music> homeRecs = getRecommendationForUser(userId);
            for (Music m : homeRecs) {
                if (result.size() >= 10) break;
                if (!addedIds.contains(m.getId())) {
                    result.add(m);
                    addedIds.add(m.getId());
                }
            }
        }

        return result;
    }

    // 游客推荐 (全站热度)
    public List<Music> getRecommendationForGuest() {
        List<Music> allMusic = fetchAllMusicWithPreference(0);
        allMusic.sort((a, b) -> Double.compare(b.getTotalPreferenceSum(), a.getTotalPreferenceSum()));
        return allMusic;
    }

    // 辅助：获取所有音乐并带上基本数据
    private List<Music> fetchAllMusicWithPreference(int userId) {
        List<Music> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname, mp.preference_value AS personal_pref " +
                    "FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "LEFT JOIN music_preference mp ON m.id = mp.music_id AND mp.user_id = ? " +
                    "WHERE m.status = 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultToMusic(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ================== ✨ 评分与反馈逻辑 (核心修复) ==================

    // 11. 隐性反馈 (时长逻辑)
    public void updateUserPreference(int userId, int musicId, int playTime, int totalTime) {
        if (totalTime <= 0) return;

        try (Connection conn = DBUtil.getConn()) {
            // 1. 检查显性状态
            String checkSql = "SELECT is_explicit, preference_value FROM music_preference WHERE user_id=? AND music_id=?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, userId);
            psCheck.setInt(2, musicId);
            ResultSet rs = psCheck.executeQuery();

            double currentScore = 0.0;
            if (rs.next()) {
                if (rs.getInt("is_explicit") == 1) return; // 显性已锁死，退出
                currentScore = rs.getDouble("preference_value");
            }

            // 2. 计算逻辑
            double ratio = (double) playTime / totalTime;
            if (ratio > 1.0) ratio = 1.0;

            double delta;
            if (ratio < 0.5) {
                // < 1/2：做减法 (减去比率)
                delta = -ratio;
            } else {
                // > 1/2：做加法 (加上比率)
                delta = ratio;
            }

            double newScore = currentScore + delta;
            if (newScore > 1.0) newScore = 1.0;
            if (newScore < -1.0) newScore = -1.0;

            // 3. 更新
            String upsertSql = "INSERT INTO music_preference (user_id, music_id, preference_value, last_exit_time, total_duration, is_explicit) " +
                    "VALUES (?, ?, ?, ?, ?, 0) " +
                    "ON DUPLICATE KEY UPDATE preference_value=?, last_exit_time=?, total_duration=?";

            PreparedStatement psUp = conn.prepareStatement(upsertSql);
            psUp.setInt(1, userId);
            psUp.setInt(2, musicId);
            psUp.setDouble(3, newScore);
            psUp.setInt(4, playTime);
            psUp.setInt(5, totalTime);
            // UPDATE 部分
            psUp.setDouble(6, newScore);
            psUp.setInt(7, playTime);
            psUp.setInt(8, totalTime);

            psUp.executeUpdate();
            updateMusicTotalPreference(musicId); // 同步总分

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 12. 显性反馈 (点赞/点踩 - 包含取消逻辑)
    public void updateUserPreferenceDirectly(int userId, int musicId, int type) {
        try (Connection conn = DBUtil.getConn()) {
            // 1. 查旧状态
            String checkSql = "SELECT preference_value, is_explicit FROM music_preference WHERE user_id=? AND music_id=?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, userId);
            psCheck.setInt(2, musicId);
            ResultSet rs = psCheck.executeQuery();

            double newScore = (double) type;
            int newExplicit = 1; // 默认锁死

            if (rs.next()) {
                double oldScore = rs.getDouble("preference_value");
                int oldExplicit = rs.getInt("is_explicit");

                // 如果已经点过赞(1)且这次又点赞(1)，或者踩同理 -> 重置为0
                if (oldExplicit == 1 && Math.abs(oldScore - type) < 0.01) {
                    newScore = 0.0;
                    newExplicit = 0; // 解锁
                }
            }

            // 2. 更新
            String sql = "INSERT INTO music_preference (user_id, music_id, preference_value, is_explicit) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE preference_value = ?, is_explicit = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, musicId);
            ps.setDouble(3, newScore);
            ps.setInt(4, newExplicit);
            ps.setDouble(5, newScore);
            ps.setInt(6, newExplicit);

            ps.executeUpdate();
            updateMusicTotalPreference(musicId);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 获取单曲评分 (用于前端高亮)
    public double getMusicPreferenceValue(int userId, int musicId) {
        double score = 0.0;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT preference_value FROM music_preference WHERE user_id=? AND music_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, musicId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) score = rs.getDouble("preference_value");
        } catch (Exception e) { e.printStackTrace(); }
        return score;
    }

    // 13. 序列记录 (A -> B)
    public void updateUserSequence(int userId, int prevMusicId, int currMusicId) {
        try (Connection conn = DBUtil.getConn()) {
            // 记录序列次数
            String sql = "INSERT INTO music_sequence_habits (user_id, current_music_id, next_music_id, occurrence_count) " +
                    "VALUES (?, ?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE occurrence_count = occurrence_count + 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, prevMusicId);
            ps.setInt(3, currMusicId);
            ps.executeUpdate();

            // 增加点击计数 (Selection Count)
            String updateMusicSql = "UPDATE music SET selection_count = selection_count + 1 WHERE id = ?";
            PreparedStatement psUp = conn.prepareStatement(updateMusicSql);
            psUp.setInt(1, currMusicId);
            psUp.executeUpdate();

            // 重新计算分数 (Selection变化)
            updateMusicTotalPreference(currMusicId);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 14. 辅助：更新全站热度
    private void updateMusicTotalPreference(int musicId) {
        try (Connection conn = DBUtil.getConn()) {
            // 算总喜好分
            String sumSql = "SELECT SUM(preference_value) FROM music_preference WHERE music_id=?";
            PreparedStatement psSum = conn.prepareStatement(sumSql);
            psSum.setInt(1, musicId);
            ResultSet rs = psSum.executeQuery();
            double totalPref = 0;
            if (rs.next()) totalPref = rs.getDouble(1);

            // 更新 music 表
            String updateSql = "UPDATE music SET total_preference_sum = ? WHERE id = ?";
            PreparedStatement psUp = conn.prepareStatement(updateSql);
            psUp.setDouble(1, totalPref);
            psUp.setInt(2, musicId);
            psUp.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 15. 辅助映射
    private Music mapResultToMusic(ResultSet rs) throws SQLException {
        Music m = new Music();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setArtist(rs.getString("artist"));
        m.setFilePath(rs.getString("file_path"));
        m.setPlayCount(rs.getInt("play_count"));
        m.setStatus(rs.getInt("status"));
        m.setUploaderName(rs.getString("uploader_name"));

        try {
            String nick = rs.getString("nickname");
            m.setUploaderNickname((nick != null && !nick.isEmpty()) ? nick : m.getUploaderName());
        } catch (SQLException e) { m.setUploaderNickname(m.getUploaderName()); }

        String d = rs.getString("duration");
        m.setDuration(d == null ? "00:00" : d);
        try { m.setUploadTime(rs.getString("upload_time")); } catch (SQLException e) {}

        try { m.setTotalPreferenceSum(rs.getDouble("total_preference_sum")); } catch (Exception e) {}
        try { m.setSelectionCount(rs.getInt("selection_count")); } catch (Exception e) {}

        try { m.setPersonalPreference(rs.getDouble("personal_pref")); } catch (Exception e) {
            m.setPersonalPreference(0.0);
        }
        return m;
    }

    // 增删改
    public void saveMusic(Music m) {
        try (Connection conn = DBUtil.getConn()) {
            // 必须保存 duration_seconds
            String sql = "INSERT INTO music(title, artist, file_path, status, uploader_name, duration, duration_seconds) VALUES(?, ?, ?, 0, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getArtist());
            ps.setString(3, m.getFilePath());
            ps.setString(4, m.getUploaderName());
            ps.setString(5, m.getDuration() == null ? "00:00" : m.getDuration());
            ps.setInt(6, m.getDurationSeconds()); // 存秒数
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