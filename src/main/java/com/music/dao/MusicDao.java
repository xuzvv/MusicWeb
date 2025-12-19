package com.music.dao;

import com.music.bean.Music;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.*;

public class MusicDao {

    // ================== 基础查询功能 ==================

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

    // ================== ✨ 核心推荐算法 (主页逻辑) ==================

    // 9. 获取首页推荐列表 (5首喜爱 + 4首习惯 + 剩余全站热度)
    public List<Music> getRecommendationForUser(int userId) {
        List<Music> finalOrder = new ArrayList<>();
        Set<Integer> usedIds = new HashSet<>();

        // 1. [Top 1-5] 用户主观喜爱 (Personal Preference)
        // 查询 music_preference 表，按分数降序
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname, mp.preference_value " +
                    "FROM music_preference mp " +
                    "JOIN music m ON mp.music_id = m.id " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE mp.user_id = ? AND m.status = 1 " +
                    "ORDER BY mp.preference_value DESC LIMIT 5";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Music m = mapResultToMusic(rs);
                m.setRecommendType("red"); // 🔥 主观喜爱 (定制)
                finalOrder.add(m);
                usedIds.add(m.getId());
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. [Top 6-9] 用户行为习惯 (Green Rocket)
        // 统计该用户在 music_sequence_habits 中，作为“下一首”出现次数最多的歌
        if (finalOrder.size() < 9) {
            try (Connection conn = DBUtil.getConn()) {
                String sql = "SELECT m.*, u.nickname, SUM(seq.occurrence_count) as total_hits " +
                        "FROM music_sequence_habits seq " +
                        "JOIN music m ON seq.next_music_id = m.id " +
                        "LEFT JOIN users u ON m.uploader_name = u.username " +
                        "WHERE seq.user_id = ? AND m.status = 1 " +
                        "GROUP BY seq.next_music_id " +
                        "ORDER BY total_hits DESC LIMIT 10";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                int count = 0;
                while (rs.next() && count < 4) {
                    int id = rs.getInt("id");
                    if (!usedIds.contains(id)) {
                        Music m = mapResultToMusic(rs);
                        m.setRecommendType("green"); // 🚀 行为习惯 (定制)
                        finalOrder.add(m);
                        usedIds.add(id);
                        count++;
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 3. [Top 10+ & 补位] 全站综合热度 (Recommendation Score)
        // 如果上面没填满 9 个，或者需要更多，都用这个补
        List<Music> allGlobal = getRecommendationForGuest(); // 复用游客的全站热度榜
        for (Music m : allGlobal) {
            if (!usedIds.contains(m.getId())) {
                // ✨✨✨ 核心修改：使用 'global_hot' 标签，代表全站补位数据 ✨✨✨
                // 这样前端就能根据这个值，给它单独设置成“白底”样式
                m.setRecommendType("global_hot");
                finalOrder.add(m);
                usedIds.add(m.getId());
            }
            if (finalOrder.size() >= 20) break; // 主页最多显示20首
        }

        return finalOrder;
    }

    // ================== ✨ 核心推荐算法 (播放页逻辑) ==================

    // 10. 获取播放页推荐列表 (序列优先 -> 全站热度补齐)
    public List<Music> getRecommendationForPlayer(int userId, int currentMusicId) {
        List<Music> result = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();
        addedIds.add(currentMusicId);

        // 1. 上下文序列 (A->B)
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
                m.setRecommendType("sequence"); // ⏭️ 序列推荐 (定制)
                result.add(m);
                addedIds.add(m.getId());
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. 全站热度补齐 (如果不足10首)
        if (result.size() < 10) {
            List<Music> globals = getRecommendationForGuest();
            for (Music m : globals) {
                if (result.size() >= 10) break;
                if (!addedIds.contains(m.getId())) {
                    m.setRecommendType("global_hot"); // ✨✨✨ 补位的标为全站热，白底
                    result.add(m);
                    addedIds.add(m.getId());
                }
            }
        }
        return result;
    }

    // 11. 游客推荐 / 全站热度榜 (按 recommendation_score 降序)
    public List<Music> getRecommendationForGuest() {
        List<Music> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.status = 1 " +
                    "ORDER BY m.recommendation_score DESC LIMIT 50";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Music m = mapResultToMusic(rs);
                m.setRecommendType("global_hot"); // ✨✨✨ 游客看到的也是全站热，白底
                list.add(m);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ================== ✨ 评分与反馈逻辑 (Auto-Like 修复) ==================

    // 12. 隐性反馈 (时长逻辑：>90% 设为 1.0)
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

            // ✨✨✨ 完播逻辑修正：>90% 直接设为 1.0 ✨✨✨
            if (ratio > 0.9) {
                currentScore = 1.0;
                // 注意：这里我们不改 is_explicit，保持为 0
                // 这样前端看到 0 就不会亮灯，但后台分数是满的
                delta = 0;
            } else {
                if (ratio < 0.5) {
                    delta = -ratio; // 扣分
                } else {
                    delta = ratio;  // 加分
                }
                currentScore += delta;
            }

            // 3. 边界限制
            if (currentScore > 1.0) currentScore = 1.0;
            if (currentScore < -1.0) currentScore = -1.0;

            // 4. 更新
            String upsertSql = "INSERT INTO music_preference (user_id, music_id, preference_value, last_exit_time, total_duration, is_explicit) " +
                    "VALUES (?, ?, ?, ?, ?, 0) " +
                    "ON DUPLICATE KEY UPDATE preference_value=?, last_exit_time=?, total_duration=?";

            PreparedStatement psUp = conn.prepareStatement(upsertSql);
            psUp.setInt(1, userId);
            psUp.setInt(2, musicId);
            psUp.setDouble(3, currentScore);
            psUp.setInt(4, playTime);
            psUp.setInt(5, totalTime);
            psUp.setDouble(6, currentScore);
            psUp.setInt(7, playTime);
            psUp.setInt(8, totalTime);

            psUp.executeUpdate();
            updateMusicTotalPreference(musicId); // 同步总分

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 13. 显性反馈 (点赞/点踩)
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

                // 取消逻辑：重复点击 -> 重置
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

    // 获取评分数值
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

    // ✨ [新增] 获取是否显性评分 (修复图标 Bug 用)
    public int getMusicExplicitStatus(int userId, int musicId) {
        int explicit = 0;
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT is_explicit FROM music_preference WHERE user_id=? AND music_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, musicId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) explicit = rs.getInt("is_explicit");
        } catch (Exception e) { e.printStackTrace(); }
        return explicit;
    }

    // 14. 序列记录
    public void updateUserSequence(int userId, int prevMusicId, int currMusicId) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO music_sequence_habits (user_id, current_music_id, next_music_id, occurrence_count) " +
                    "VALUES (?, ?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE occurrence_count = occurrence_count + 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, prevMusicId);
            ps.setInt(3, currMusicId);
            ps.executeUpdate();

            // 增加 Selection Count
            String updateMusicSql = "UPDATE music SET selection_count = selection_count + 1 WHERE id = ?";
            PreparedStatement psUp = conn.prepareStatement(updateMusicSql);
            psUp.setInt(1, currMusicId);
            psUp.executeUpdate();

            updateMusicTotalPreference(currMusicId);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 15. 更新全站热度
    private void updateMusicTotalPreference(int musicId) {
        try (Connection conn = DBUtil.getConn()) {
            // 1. 算主观总分
            String sumSql = "SELECT SUM(preference_value) FROM music_preference WHERE music_id=?";
            PreparedStatement psSum = conn.prepareStatement(sumSql);
            psSum.setInt(1, musicId);
            ResultSet rs = psSum.executeQuery();
            double totalPref = 0;
            if (rs.next()) totalPref = rs.getDouble(1);

            // 2. 算综合分 (主观 + 0.1*点击)
            String selSql = "SELECT selection_count FROM music WHERE id=?";
            PreparedStatement psSel = conn.prepareStatement(selSql);
            psSel.setInt(1, musicId);
            ResultSet rsSel = psSel.executeQuery();
            int selCount = 0;
            if (rsSel.next()) selCount = rsSel.getInt(1);

            double recScore = totalPref + (selCount * 0.1);

            // 3. 更新
            String updateSql = "UPDATE music SET total_preference_sum = ?, recommendation_score = ? WHERE id = ?";
            PreparedStatement psUp = conn.prepareStatement(updateSql);
            psUp.setDouble(1, totalPref);
            psUp.setDouble(2, recScore);
            psUp.setInt(3, musicId);
            psUp.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Music mapResultToMusic(ResultSet rs) throws SQLException {
        Music m = new Music();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setArtist(rs.getString("artist"));
        m.setFilePath(rs.getString("file_path"));
        m.setPlayCount(rs.getInt("play_count"));
        m.setStatus(rs.getInt("status"));
        m.setUploaderName(rs.getString("uploader_name"));
        try { m.setUploaderNickname(rs.getString("nickname")!=null?rs.getString("nickname"):m.getUploaderName()); } catch (Exception e) { m.setUploaderNickname(m.getUploaderName()); }
        m.setDuration(rs.getString("duration"));
        try { m.setUploadTime(rs.getString("upload_time")); } catch (Exception e) {}
        try { m.setTotalPreferenceSum(rs.getDouble("total_preference_sum")); } catch (Exception e) {}
        try { m.setSelectionCount(rs.getInt("selection_count")); } catch (Exception e) {}
        return m;
    }

    // 基础增删改查
    public void saveMusic(Music m) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO music(title, artist, file_path, status, uploader_name, duration, duration_seconds) VALUES(?, ?, ?, 0, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getArtist());
            ps.setString(3, m.getFilePath());
            ps.setString(4, m.getUploaderName());
            ps.setString(5, m.getDuration() == null ? "00:00" : m.getDuration());
            ps.setInt(6, m.getDurationSeconds());
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
    public List<Music> getHotMusic() {
        return getMusicList("hot", 1, 10);
    }
}