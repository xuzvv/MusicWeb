package com.music.controller;

import com.google.gson.Gson; // 需要你在pom.xml引入了Gson
import com.music.bean.Danmaku;
import com.music.dao.DanmakuDao;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@ServerEndpoint("/danmaku/{musicId}")
public class DanmakuServer {
    private static Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();
    private DanmakuDao dao = new DanmakuDao(); // 引入 DAO

    @OnOpen
    public void onOpen(Session session, @PathParam("musicId") String musicId) {
        rooms.computeIfAbsent(musicId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("musicId") String musicId) {
        Set<Session> room = rooms.get(musicId);
        if (room != null) room.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("musicId") String musicId) {
        // 1. 解析前端发来的 JSON: {"text":"666", "time":12.5}
        Gson gson = new Gson();
        MapData data = gson.fromJson(message, MapData.class);

        // 2. 保存到数据库
        Danmaku d = new Danmaku();
        d.setMusicId(Integer.parseInt(musicId));
        d.setContent(data.text);
        d.setVideoTime(data.time);
        dao.save(d); // 持久化！

        // 3. 广播给房间所有人 (原样转发JSON)
        Set<Session> room = rooms.get(musicId);
        if (room != null) {
            for (Session s : room) {
                try {
                    s.getBasicRemote().sendText(message);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) { error.printStackTrace(); }

    // 内部类用于解析JSON
    class MapData {
        String text;
        double time;
    }
}