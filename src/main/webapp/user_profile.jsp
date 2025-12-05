<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.User" %>
<%@ page import="com.music.bean.Music" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>个人主页 - Echo · 回声</title>
    <style>
        body { margin: 0; padding: 0; font-family: "Microsoft YaHei", sans-serif; background: #f4f6f9; color: #333; padding-top: 40px; padding-bottom: 40px; }
        .container { width: 1000px; margin: 0 auto; display: flex; gap: 25px; align-items: flex-start; }
        .profile-card { flex: 1; background: white; padding: 30px 20px; border-radius: 12px; text-align: center; box-shadow: 0 10px 30px rgba(0,0,0,0.05); position: sticky; top: 20px; }
        .avatar { width: 130px; height: 130px; border-radius: 50%; object-fit: cover; border: 4px solid #f8f9fa; margin-bottom: 15px; }
        .username { font-size: 22px; font-weight: bold; margin-bottom: 5px; }
        .handle { font-size: 14px; color: #888; margin-bottom: 20px; }
        .bio-box { background: #f8f9fa; padding: 15px; border-radius: 8px; text-align: left; font-size: 14px; color: #555; margin-bottom: 20px; border-left: 3px solid #764ba2; }
        .btn-action { display: block; width: 100%; padding: 10px 0; border-radius: 6px; font-size: 15px; font-weight: bold; margin-bottom: 10px; cursor: pointer; border: none; text-decoration: none; text-align: center;}
        .btn-edit { background: #e9ecef; color: #495057; }
        .btn-chat { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
        .btn-inbox { background: #17a2b8; color: white; } /* 新增：消息箱按钮颜色 */

        .works-area { flex: 3; background: white; padding: 0 30px 30px 30px; border-radius: 12px; min-height: 500px; }
        .section-header { padding: 25px 0; border-bottom: 1px solid #eee; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }
        .section-title { font-size: 20px; font-weight: bold; }
        .music-item { display: flex; justify-content: space-between; align-items: center; padding: 20px; border-bottom: 1px solid #f9f9f9; }
        .btn-play { border: 1px solid #764ba2; color: #764ba2; padding: 5px 15px; border-radius: 20px; font-size: 13px; text-decoration: none;}
        .btn-play:hover { background: #764ba2; color: white; }
    </style>
</head>
<body>
<%
    User target = (User)request.getAttribute("targetUser");
    User current = (User)session.getAttribute("user");
    boolean isMe = (current != null && target != null && current.getId() == target.getId());
    List<Music> list = (List<Music>)request.getAttribute("musicList");
    int count = (list != null) ? list.size() : 0;
    if(target == null) { response.sendRedirect("index"); return; }
%>

<div class="container">
    <div class="profile-card">
        <img src="<%= target.getAvatar() %>" class="avatar">
        <div class="username"><%= target.getNickname() != null ? target.getNickname() : target.getUsername() %></div>
        <div class="handle">ID: <%= target.getUsername() %></div>
        <div class="bio-box"><%= target.getBio() == null || target.getBio().isEmpty() ? "暂无简介" : target.getBio() %></div>

        <% if(isMe) { %>
        <a href="user_edit.jsp" class="btn-action btn-edit">✏️ 编辑资料</a>
        <a href="messageList" class="btn-action btn-inbox">📩 我的消息箱</a>
        <% } else { %>
        <a href="chatPage?targetId=<%= target.getId() %>&username=<%= target.getUsername() %>" class="btn-action btn-chat">💬 发送私信</a>
        <% } %>

        <a href="index" style="display:block; margin-top:10px; color:#999; font-size:13px;">← 返回首页</a>
    </div>

    <div class="works-area">
        <div class="section-header">
            <span class="section-title">📂 发布的音乐作品</span>
            <span style="background:#eee; padding:2px 8px; border-radius:10px; font-size:12px;"><%= count %></span>
        </div>
        <% if(count > 0) {
            for(Music m : list) { %>
        <div class="music-item">
            <div>
                <div style="font-weight:bold; font-size:16px;"><%= m.getTitle() %></div>
                <div style="font-size:13px; color:#999; margin-top:5px;">
                    📅 <%= m.getUploadTime() != null && m.getUploadTime().length()>=10 ? m.getUploadTime().substring(0,10) : "未知" %>
                    &nbsp;|&nbsp; 👂 <%= m.getPlayCount() %> 播放
                </div>
            </div>
            <div>
                <span style="font-family:monospace; color:#ccc; margin-right:15px;"><%= m.getDuration() %></span>
                <a href="play?id=<%= m.getId() %>" class="btn-play">▶ 播放</a>
            </div>
        </div>
        <% }} else { %>
        <div style="text-align:center; padding:60px 0; color:#999;">暂无作品</div>
        <% } %>
    </div>
</div>
</body>
</html>