package com.music.dao;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InviteDao {
    // 检查并使用
    public boolean checkAndUseCode(String code) {
        boolean isValid = false;
        try (Connection conn = DBUtil.getConn()) {
            String sqlCheck = "SELECT id FROM invite_codes WHERE code = ? AND is_used = 0";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setString(1, code);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String sqlUpdate = "UPDATE invite_codes SET is_used = 1 WHERE id = ?";
                PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
                psUpdate.setInt(1, id);
                psUpdate.executeUpdate();
                isValid = true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return isValid;
    }

    // ✨✨✨ 新增：获取所有邀请码 (供后台展示) ✨✨✨
    // 为了简单，我们用 Map<String, Object> 来存每一行数据
    public List<Map<String, Object>> getAllCodes() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT * FROM invite_codes ORDER BY id DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("code", rs.getString("code"));
                map.put("isUsed", rs.getInt("is_used"));
                list.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ✨✨✨ 新增：添加邀请码 ✨✨✨
    public void addCode(String code) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO invite_codes(code, is_used) VALUES(?, 0)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ✨✨✨ 新增：删除邀请码 ✨✨✨
    public void deleteCode(int id) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "DELETE FROM invite_codes WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}