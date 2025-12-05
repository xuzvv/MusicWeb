<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>我的消息 - Echo</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; background: #f4f6f9; padding: 20px; }
        .container { width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
        .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 20px; }
        .header h2 { margin: 0; color: #333; }
        .btn-home { text-decoration: none; color: #666; font-size: 14px; }

        .msg-item { display: flex; align-items: center; padding: 15px; border-bottom: 1px solid #f9f9f9; transition: 0.2s; cursor: pointer; text-decoration: none; color: inherit; }
        .msg-item:hover { background: #f0f8ff; transform: translateX(5px); }
        .avatar { width: 50px; height: 50px; border-radius: 50%; object-fit: cover; margin-right: 15px; border: 1px solid #eee; }
        .info { flex: 1; }
        .name { font-weight: bold; font-size: 16px; color: #333; }
        .desc { font-size: 13px; color: #888; margin-top: 5px; }
        .badge { background: #ff4757; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: bold; }
        .empty { text-align: center; color: #999; padding: 50px; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>💬 消息中心</h2>
        <div>
            <a href="user_profile.jsp" class="btn-home">返回个人主页</a> |
            <a href="index" class="btn-home">返回首页</a>
        </div>
    </div>

    <div class="list-box">
        <%
            List<Map<String, Object>> list = (List<Map<String, Object>>)request.getAttribute("contactList");
            if (list != null && list.size() > 0) {
                for(Map<String, Object> map : list) {
                    User contact = (User)map.get("contact");
                    int unread = (Integer)map.get("unread");
        %>
        <a href="chatPage?targetId=<%= contact.getId() %>&username=<%= contact.getUsername() %>" class="msg-item">
            <img src="<%= contact.getAvatar() %>" class="avatar">
            <div class="info">
                <div class="name"><%= contact.getNickname()!=null ? contact.getNickname() : contact.getUsername() %></div>
                <div class="desc">点击查看聊天记录...</div>
            </div>
            <% if(unread > 0) { %>
            <span class="badge"><%= unread %></span>
            <% } %>
            <span style="color:#ccc; font-size:20px; margin-left:15px;">&rsaquo;</span>
        </a>
        <%
            }
        } else {
        %>
        <div class="empty">
            <h3>📭 暂无消息</h3>
            <p>你的信箱空空如也，快去给创作者发私信吧！</p>
        </div>
        <% } %>
    </div>
</div>
</body>
</html>