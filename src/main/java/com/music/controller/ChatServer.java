package com.music.controller;

import com.google.gson.Gson;
import com.music.bean.Message;
import com.music.dao.MessageDao;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// URL格式: ws://localhost:8080/MusicWeb/chatSocket/{userId}
@ServerEndpoint("/chatSocket/{userId}")
public class ChatServer {

    // 关键：记录所有在线用户 (Key: userId, Value: Session)
    private static Map<Integer, Session> onlineUsers = new ConcurrentHashMap<>();
    private MessageDao messageDao = new MessageDao();
    private Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        int userId = Integer.parseInt(userIdStr);
        onlineUsers.put(userId, session);
        System.out.println("用户上线: " + userId);
    }

    @OnClose
    public void onClose(@PathParam("userId") String userIdStr) {
        int userId = Integer.parseInt(userIdStr);
        onlineUsers.remove(userId);
        System.out.println("用户下线: " + userId);
    }

    @OnMessage
    public void onMessage(String messageJson, Session session) {
        // 1. 解析前端发来的 JSON 数据
        // 格式: { "senderId": 1, "receiverId": 2, "content": "你好" }
        Message msg = gson.fromJson(messageJson, Message.class);

        // 2. 保存到数据库 (持久化)
        messageDao.saveMessage(msg);

        // 3. 尝试发送给接收者 (如果在线)
        Session receiverSession = onlineUsers.get(msg.getReceiverId());
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                // 直接转发 JSON
                receiverSession.getBasicRemote().sendText(gson.toJson(msg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("用户 " + msg.getReceiverId() + " 不在线，消息已存库");
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}