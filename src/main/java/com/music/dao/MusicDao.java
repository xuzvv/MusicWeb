package com.music.dao;

import com.music.bean.Music;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.*;

public class MusicDao {

    // ================== 基础查询功能 ==================

    // 1. 通用分页查询 (首页用 - 热门/最新)
    public List<Music> getMusicList(String type, int page, int size) {
        List<Music> list = new ArrayList<>();
        int offset = (page - 1) * size;
        String orderBy = "m.play_count DESC";
        if ("new".equals(type)) {
            orderBy = "m.upload_time DESC";
        } else if ("random".equals(type)) {
            // random 逻辑现已在 HomeServlet 中接管，但在 DAO 层保留此基础实现以防万一
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

    // 6. 随机推荐 (保留旧接口兼容性)
    public List<Music> getRandomMusicList(int size) {
        return getMusicList("random", 1, size);
    }

    // 7. 获取待审核音乐
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

    // 8. 获取所有音乐
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

    // ================== ✨ 推荐算法读取部分 ✨ ==================

    // 9. 获取登录用户的推荐列表 (算法核心逻辑)
    public List<Music> getRecommendationForUser(int userId) {
        List<Music> allMusic = new ArrayList<>();

        // 1. 获取所有上架音乐，并关联当前用户的个人喜好度 (personal_pref)
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname, mp.preference_value AS personal_pref " +
                    "FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "LEFT JOIN music_preference mp ON m.id = mp.music_id AND mp.user_id = ? " +
                    "WHERE m.status = 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                allMusic.add(mapResultToMusic(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (allMusic.isEmpty()) return allMusic;

        // --- 算法逻辑开始 ---

        // 2. 找出 Top 4 喜爱度 (RED 候选: 全站总喜爱度)
        List<Music> sortedByPref = new ArrayList<>(allMusic);
        sortedByPref.sort((a, b) -> Double.compare(b.getTotalPreferenceSum(), a.getTotalPreferenceSum()));
        Set<Integer> redIds = new HashSet<>();
        for (int i = 0; i < Math.min(4, sortedByPref.size()); i++) {
            redIds.add(sortedByPref.get(i).getId());
        }

        // 3. 找出 Top 3 点击次数 (GREEN 候选: 被选取次数)
        List<Music> sortedBySelect = new ArrayList<>(allMusic);
        sortedBySelect.sort((a, b) -> Integer.compare(b.getSelectionCount(), a.getSelectionCount()));
        Set<Integer> greenIds = new HashSet<>();
        for (int i = 0; i < Math.min(3, sortedBySelect.size()); i++) {
            greenIds.add(sortedBySelect.get(i).getId());
        }

        // 4. 分类构建列表
        List<Music> redList = new ArrayList<>();
        List<Music> greenList = new ArrayList<>();
        List<Music> restList = new ArrayList<>();

        for (Music m : allMusic) {
            boolean isRed = redIds.contains(m.getId());
            boolean isGreen = greenIds.contains(m.getId());

            if (isRed && isGreen) {
                m.setRecommendType("mixed"); // 红绿混合
                redList.add(m); // 优先放入红色区域显示
            } else if (isRed) {
                m.setRecommendType("red");
                redList.add(m);
            } else if (isGreen) {
                m.setRecommendType("green");
                greenList.add(m);
            } else {
                m.setRecommendType("normal");
                restList.add(m);
            }
        }

        // 5. 组内排序
        redList.sort((a, b) -> Double.compare(b.getTotalPreferenceSum(), a.getTotalPreferenceSum()));
        greenList.sort((a, b) -> Integer.compare(b.getSelectionCount(), a.getSelectionCount()));
        restList.sort((a, b) -> Double.compare(b.getPersonalPreference(), a.getPersonalPreference()));

        // 6. 合并最终列表
        List<Music> finalOrder = new ArrayList<>();
        finalOrder.addAll(redList);
        finalOrder.addAll(greenList);
        finalOrder.addAll(restList);

        return finalOrder;
    }

    // 10. 获取游客的推荐列表 (全站热度 + 随机)
    public List<Music> getRecommendationForGuest() {
        List<Music> allMusic = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.nickname FROM music m " +
                    "LEFT JOIN users u ON m.uploader_name = u.username " +
                    "WHERE m.status = 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                allMusic.add(mapResultToMusic(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (allMusic.isEmpty()) return allMusic;

        // 1. 排序：全站点击次数 Top 10 (GREEN)
        allMusic.sort((a, b) -> Integer.compare(b.getSelectionCount(), a.getSelectionCount()));

        // 2. 标记 Top 10 为绿色
        for (int i = 0; i < allMusic.size(); i++) {
            if (i < 10) {
                allMusic.get(i).setRecommendType("green");
            } else {
                allMusic.get(i).setRecommendType("normal");
            }
        }

        // 3. 随机逻辑 (不满一页用随机)
        if (allMusic.size() > 10) {
            List<Music> head = allMusic.subList(0, 10);
            List<Music> tail = new ArrayList<>(allMusic.subList(10, allMusic.size()));
            Collections.shuffle(tail);

            List<Music> result = new ArrayList<>(head);
            result.addAll(tail);
            return result;
        }
        return allMusic;
    }

    // ================== ✨ 推荐算法写入部分 (核心更新逻辑) ✨ ==================

    // 11. ✨ 核心算法 1：隐性反馈 (最大绝对值逻辑 + 优先级判断)
    public void updateUserPreference(int userId, int musicId, int playTime, int totalTime) {
        if (totalTime <= 0) return;

        // 1. 计算比例
        double ratio = (double) playTime / totalTime;
        if (ratio > 1.0) ratio = 1.0;

        // 2. 计算本次行为的“态度分”
        // 逻辑：听得少(<0.5)是负分，听得多(>=0.5)是正分
        double newScore;
        if (ratio < 0.5) {
            // 听得越少，负分越 heavy (例: 0.1 -> -0.9)
            newScore = -1.0 * (1.0 - ratio);
        } else {
            // 听得越多，正分越高 (例: 0.9 -> 0.9)
            newScore = ratio;
        }

        try (Connection conn = DBUtil.getConn()) {
            // 3. 查旧账：获取当前分数和是否显性
            String checkSql = "SELECT preference_value, is_explicit FROM music_preference WHERE user_id=? AND music_id=?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, userId);
            psCheck.setInt(2, musicId);
            ResultSet rs = psCheck.executeQuery();

            double oldScore = 0.0;
            boolean isExplicit = false; // 默认为隐性

            if (rs.next()) {
                oldScore = rs.getDouble("preference_value");
                // 兼容处理：如果数据库旧数据是 null，视为 0 (隐性)
                int explicitVal = rs.getObject("is_explicit") != null ? rs.getInt("is_explicit") : 0;
                isExplicit = (explicitVal == 1);
            }

            // ✨✨✨ 核心逻辑判断 ✨✨✨

            // 规则A：显性优先。如果用户以前点过赞/踩，本次时长数据直接忽略。
            if (isExplicit) {
                return;
            }

            // 规则B：最大绝对值原则。
            // 只有当“新行为的强烈程度” > “旧行为的强烈程度”时，才更新。
            if (Math.abs(newScore) > Math.abs(oldScore)) {
                // 执行更新 (隐性评分 is_explicit = 0)
                String upsertSql = "INSERT INTO music_preference (user_id, music_id, preference_value, last_exit_time, total_duration, is_explicit) " +
                        "VALUES (?, ?, ?, ?, ?, 0) " +
                        "ON DUPLICATE KEY UPDATE preference_value=?, last_exit_time=?, total_duration=?, is_explicit=0";

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

                // 同步更新全站热度
                updateMusicTotalPreference(musicId);
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 12. ✨ 核心算法 2：显性反馈 (点赞/点踩) - 强制覆盖并加锁
    public void updateUserPreferenceDirectly(int userId, int musicId, double score) {
        try (Connection conn = DBUtil.getConn()) {
            // is_explicit = 1 (锁死)，无论之前是啥，直接覆盖
            String sql = "INSERT INTO music_preference (user_id, music_id, preference_value, is_explicit) VALUES (?, ?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE preference_value = ?, is_explicit = 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, musicId);
            ps.setDouble(3, score);
            ps.setDouble(4, score);
            ps.executeUpdate();

            updateMusicTotalPreference(musicId);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 13. ✨ 核心算法 3：序列记录 (A -> B)
    public void updateUserSequence(int userId, int prevMusicId, int currMusicId) {
        try (Connection conn = DBUtil.getConn()) {
            // 1. 插入序列记录 (如果已存在则次数+1)
            String sql = "INSERT INTO music_sequence_habits (user_id, current_music_id, next_music_id, occurrence_count) " +
                    "VALUES (?, ?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE occurrence_count = occurrence_count + 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, prevMusicId);
            ps.setInt(3, currMusicId);
            ps.executeUpdate();

            // 2. 给下一首 (currMusicId) 的 selection_count + 1
            String updateMusicSql = "UPDATE music SET selection_count = selection_count + 1, " +
                    "recommendation_score = total_preference_sum + ((selection_count + 1) * 0.1) " +
                    "WHERE id = ?";
            PreparedStatement psUp = conn.prepareStatement(updateMusicSql);
            psUp.setInt(1, currMusicId);
            psUp.executeUpdate();

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 14. 辅助：更新全站热度 (Sync)
    private void updateMusicTotalPreference(int musicId) {
        try (Connection conn = DBUtil.getConn()) {
            String sumSql = "SELECT SUM(preference_value) FROM music_preference WHERE music_id=?";
            PreparedStatement psSum = conn.prepareStatement(sumSql);
            psSum.setInt(1, musicId);
            ResultSet rs = psSum.executeQuery();
            double totalPref = 0;
            if (rs.next()) totalPref = rs.getDouble(1);

            String updateSql = "UPDATE music SET total_preference_sum = ?, recommendation_score = ? + (selection_count * 0.1) WHERE id = ?";
            PreparedStatement psUp = conn.prepareStatement(updateSql);
            psUp.setDouble(1, totalPref);
            psUp.setDouble(2, totalPref);
            psUp.setInt(3, musicId);
            psUp.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 15. 辅助映射 (增强版：支持读取算法字段)
    private Music mapResultToMusic(ResultSet rs) throws SQLException {
        Music m = new Music();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setArtist(rs.getString("artist"));
        m.setFilePath(rs.getString("file_path"));
        m.setPlayCount(rs.getInt("play_count"));
        m.setStatus(rs.getInt("status"));
        m.setUploaderName(rs.getString("uploader_name"));

        // 读取 nickname
        try {
            String nick = rs.getString("nickname");
            m.setUploaderNickname((nick != null && !nick.isEmpty()) ? nick : m.getUploaderName());
        } catch (SQLException e) {
            m.setUploaderNickname(m.getUploaderName());
        }

        String d = rs.getString("duration");
        m.setDuration(d == null ? "00:00" : d);
        try { m.setUploadTime(rs.getString("upload_time")); } catch (SQLException e) {}

        // 读取新算法字段
        try { m.setTotalPreferenceSum(rs.getDouble("total_preference_sum")); } catch (Exception e) {}
        try { m.setSelectionCount(rs.getInt("selection_count")); } catch (Exception e) {}
        try { m.setRecommendationScore(rs.getDouble("recommendation_score")); } catch (Exception e) {}

        // 读取个人喜好度
        try { m.setPersonalPreference(rs.getDouble("personal_pref")); } catch (Exception e) {
            m.setPersonalPreference(0.0);
        }

        return m;
    }

    // ================== 基础增删改操作 ==================
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