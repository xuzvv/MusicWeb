<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/5
  Time: 22:34
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.User" %>
<%@ page import="com.music.bean.Message" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>与 ${targetUser.nickname} 聊天中</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; background: #f0f2f5; display: flex; justify-content: center; padding-top: 30px; height: 90vh; overflow: hidden; }
        .chat-container { width: 800px; background: white; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.1); display: flex; flex-direction: column; overflow: hidden; }
        .chat-header { padding: 15px 20px; border-bottom: 1px solid #eee; background: #f9f9f9; display: flex; justify-content: space-between; align-items: center; }
        .chat-header h3 { margin: 0; font-size: 16px; }
        .chat-header a { text-decoration: none; color: #666; font-size: 14px; }

        .chat-body { flex: 1; padding: 20px; overflow-y: auto; background: #fff; }
        .message-row { display: flex; margin-bottom: 20px; align-items: flex-start; }
        .message-row.self { flex-direction: row-reverse; }

        .avatar { width: 40px; height: 40px; border-radius: 50%; margin: 0 10px; }

        /* 消息内容包装器 */
        .msg-content { display: flex; flex-direction: column; max-width: 60%; }
        .message-row.self .msg-content { align-items: flex-end; } /* 自己发的消息靠右对齐 */

        .bubble { padding: 10px 15px; border-radius: 10px; font-size: 14px; line-height: 1.5; position: relative; word-wrap: break-word;}
        .message-row:not(.self) .bubble { background: #f0f0f0; color: #333; border-top-left-radius: 0; }
        .message-row.self .bubble { background: #007bff; color: white; border-top-right-radius: 0; }

        /* ✨ 新增：时间样式 ✨ */
        .time { font-size: 12px; color: #bbb; margin-top: 5px; margin-left: 2px; }

        .chat-footer { padding: 15px; border-top: 1px solid #eee; background: #f9f9f9; display: flex; gap: 10px; }
        input { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 5px; outline: none; }
        button { padding: 0 25px; background: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; font-weight: bold; }
    </style>
</head>
<body>
<%
    User current = (User)session.getAttribute("user");
    User target = (User)request.getAttribute("targetUser");
%>

<div class="chat-container">
    <div class="chat-header">
        <h3>💬 与 <%= target.getNickname()!=null?target.getNickname():target.getUsername() %> 聊天中</h3>
        <a href="profile?username=<%= target.getUsername() %>">查看主页</a>
    </div>

    <div class="chat-body" id="msgBox">
        <%
            List<Message> list = (List<Message>)request.getAttribute("history");
            if(list != null) {
                for(Message m : list) {
                    boolean isSelf = (m.getSenderId() == current.getId());
        %>
        <div class="message-row <%= isSelf ? "self" : "" %>">
            <img src="<%= m.getSenderAvatar() %>" class="avatar">
            <div class="msg-content">
                <div class="bubble"><%= m.getContent() %></div>
                <div class="time"><%= m.getSendTime() %></div>
            </div>
        </div>
        <% }} %>
    </div>

    <div class="chat-footer">
        <input type="text" id="inputMsg" placeholder="输入消息... (回车发送)" />
        <button onclick="sendMsg()">发送</button>
    </div>
</div>

<script>
    var currentId = <%= current.getId() %>;
    var targetId = <%= target.getId() %>;
    var currentAvatar = "<%= current.getAvatar() %>";
    var targetAvatar = "<%= target.getAvatar() %>";
    var contextPath = "<%= request.getContextPath() %>";

    var ws = new WebSocket("ws://" + window.location.host + contextPath + "/chatSocket/" + currentId);

    ws.onmessage = function(event) {
        var msg = JSON.parse(event.data);
        if (msg.senderId === targetId) {
            appendMsg(msg.content, false);
        }
    };

    function sendMsg() {
        var input = document.getElementById("inputMsg");
        var text = input.value.trim();
        if(!text) return;

        var msgObj = {
            senderId: currentId,
            receiverId: targetId,
            content: text
        };
        ws.send(JSON.stringify(msgObj));
        appendMsg(text, true);
        input.value = "";
    }

    document.getElementById("inputMsg").addEventListener("keypress", function(e){ if(e.key === 'Enter') sendMsg(); });

    function appendMsg(text, isSelf) {
        var box = document.getElementById("msgBox");
        var row = document.createElement("div");
        row.className = "message-row " + (isSelf ? "self" : "");

        var img = document.createElement("img");
        img.className = "avatar";
        img.src = isSelf ? currentAvatar : targetAvatar;

        var contentDiv = document.createElement("div");
        contentDiv.className = "msg-content";

        var bubble = document.createElement("div");
        bubble.className = "bubble";
        bubble.innerText = text;

        // 获取当前时间
        var timeDiv = document.createElement("div");
        timeDiv.className = "time";
        var now = new Date();
        timeDiv.innerText = now.getHours() + ":" + String(now.getMinutes()).padStart(2, '0');

        contentDiv.appendChild(bubble);
        contentDiv.appendChild(timeDiv);

        row.appendChild(img);
        row.appendChild(contentDiv);
        box.appendChild(row);

        box.scrollTop = box.scrollHeight;
    }

    window.onload = function() {
        var box = document.getElementById("msgBox");
        box.scrollTop = box.scrollHeight;
    };
</script>
</body>
</html>