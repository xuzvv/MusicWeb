package com.music.dao;

import com.music.bean.Message;
import com.music.bean.User;
import com.music.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDao {

    // 1. 保存消息 (加入了详细的调试日志)
    public void saveMessage(Message msg) {
        System.out.println("【Debug】开始保存消息 -> 发送者:" + msg.getSenderId() + " 接收者:" + msg.getReceiverId() + " 内容:" + msg.getContent());

        try (Connection conn = DBUtil.getConn()) {
            String sql = "INSERT INTO messages(sender_id, receiver_id, content) VALUES(?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, msg.getSenderId());
            ps.setInt(2, msg.getReceiverId());
            ps.setString(3, msg.getContent());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("【Debug】✅ 消息保存成功！");
            } else {
                System.out.println("【Debug】❌ 消息保存失败，影响行数为 0");
            }
        } catch (Exception e) {
            System.out.println("【Debug】❌ 发生严重错误，保存失败！！错误详情如下：");
            e.printStackTrace(); // 请在 IDEA 下方的控制台看这里的红字报错
        }
    }

    // 2. 获取聊天记录
    public List<Message> getChatHistory(int userId1, int userId2) {
        List<Message> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT m.*, u.username, u.nickname, u.avatar " +
                    "FROM messages m " +
                    "JOIN users u ON m.sender_id = u.id " +
                    "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                    "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                    "ORDER BY m.send_time ASC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setSenderId(rs.getInt("sender_id"));
                m.setReceiverId(rs.getInt("receiver_id"));
                m.setContent(rs.getString("content"));

                String time = rs.getString("send_time");
                if(time != null && time.length() > 16) time = time.substring(0, 16);
                m.setSendTime(time);

                m.setIsRead(rs.getInt("is_read"));
                String nick = rs.getString("nickname");
                m.setSenderName(nick != null ? nick : rs.getString("username"));
                m.setSenderAvatar(rs.getString("avatar"));
                list.add(m);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. 标记已读
    public void markAsRead(int senderId, int receiverId) {
        try (Connection conn = DBUtil.getConn()) {
            String sql = "UPDATE messages SET is_read = 1 WHERE sender_id = ? AND receiver_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 4. 获取最近联系人
    public List<Map<String, Object>> getRecentContacts(int myId) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConn()) {
            String sql = "SELECT DISTINCT u.* FROM users u " +
                    "JOIN messages m ON u.id = m.sender_id OR u.id = m.receiver_id " +
                    "WHERE (m.sender_id = ? OR m.receiver_id = ?) AND u.id != ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, myId);
            ps.setInt(2, myId);
            ps.setInt(3, myId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                User contact = new User();
                contact.setId(rs.getInt("id"));
                contact.setUsername(rs.getString("username"));
                contact.setNickname(rs.getString("nickname"));
                contact.setAvatar(rs.getString("avatar"));
                if(contact.getAvatar() == null) contact.setAvatar("https://api.dicebear.com/7.x/identicon/svg?seed=" + contact.getUsername());

                map.put("contact", contact);
                map.put("unread", countUnread(conn, rs.getInt("id"), myId));
                list.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private int countUnread(Connection conn, int senderId, int receiverId) throws SQLException {
        String sql = "SELECT count(*) FROM messages WHERE sender_id = ? AND receiver_id = ? AND is_read = 0";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, senderId);
        ps.setInt(2, receiverId);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) return rs.getInt(1);
        return 0;
    }
}