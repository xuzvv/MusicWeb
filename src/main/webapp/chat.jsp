<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.User" %>
<%@ page import="com.music.bean.Message" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

        /* 消息行基础样式 */
        .message-row { display: flex; margin-bottom: 20px; align-items: flex-start; }

        /* 对方的消息 (左侧) */
        .message-row.left { flex-direction: row; }

        /* 自己的消息 (右侧) */
        .message-row.right { flex-direction: row-reverse; }

        .avatar { width: 40px; height: 40px; border-radius: 50%; margin: 0 10px; }

        .msg-content { display: flex; flex-direction: column; max-width: 60%; }
        .message-row.right .msg-content { align-items: flex-end; }

        .bubble { padding: 10px 15px; border-radius: 10px; font-size: 14px; line-height: 1.5; word-wrap: break-word; position: relative;}

        /* 左侧气泡颜色 */
        .message-row.left .bubble { background: #f0f0f0; color: #333; border-top-left-radius: 0; }

        /* 右侧气泡颜色 */
        .message-row.right .bubble { background: #007bff; color: white; border-top-right-radius: 0; }

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
        <c:forEach items="${history}" var="m">
            <c:choose>
                <%-- 己方消息 (右侧) --%>
                <c:when test="${m.senderId == sessionScope.user.id}">
                    <div class="message-row right">
                        <img src="${m.senderAvatar}" class="avatar">
                        <div class="msg-content">
                            <div class="bubble">${m.content}</div>
                            <div class="time">${m.sendTime}</div>
                        </div>
                    </div>
                </c:when>

                <%-- 对方消息 (左侧) --%>
                <c:otherwise>
                    <div class="message-row left">
                        <img src="${m.senderAvatar}" class="avatar">
                        <div class="msg-content">
                            <div class="bubble">${m.content}</div>
                            <div class="time">${m.sendTime}</div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:forEach>
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

    // === 修改开始：自动适配 HTTPS 协议 ===
    var wsProtocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    var wsUrl = wsProtocol + window.location.host + contextPath + "/chatSocket/" + currentId;

    var ws = new WebSocket(wsUrl);
    // === 修改结束 ===

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

        // 增加安全检查：如果连接没建立，提示用户
        if (!ws || ws.readyState !== WebSocket.OPEN) {
            alert("聊天服务连接中或连接失败，请刷新页面重试！");
            return;
        }

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
        // 根据 isSelf 决定添加 left 还是 right 类
        row.className = "message-row " + (isSelf ? "right" : "left");

        var img = document.createElement("img");
        img.className = "avatar";
        img.src = isSelf ? currentAvatar : targetAvatar;

        var contentDiv = document.createElement("div");
        contentDiv.className = "msg-content";

        var bubble = document.createElement("div");
        bubble.className = "bubble";
        bubble.innerText = text;

        var timeDiv = document.createElement("div");
        timeDiv.className = "time";

        // 实时消息使用浏览器当前时间
        var now = new Date();
        var mins = String(now.getMinutes()).padStart(2, '0');
        // 简单显示 HH:mm
        timeDiv.innerText = now.getHours() + ":" + mins;

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