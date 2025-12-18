<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.Music" %>
<%@ page import="com.music.bean.User" %>
<%@ page import="com.music.bean.Comment" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>正在播放 - X² Voice</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/aplayer/1.10.1/APlayer.min.css">

    <style>
        body { background: #222; color: #fff; font-family: "Microsoft YaHei", sans-serif; margin: 0; padding-top: 50px; }

        /* 页面主体容器：Flex左右布局 */
        .container {
            width: 1000px; /* 加宽以容纳侧边栏 */
            margin: 0 auto;
            padding-bottom: 80px;
            display: flex;
            gap: 20px;
            align-items: flex-start;
        }

        /* === 左侧：播放器主体 (占 70%) === */
        .player-left { flex: 7; }
        .player-card { background: #333; padding: 40px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); position: relative;}

        /* === 右侧：推荐栏 (占 30%) === */
        .recommend-box {
            flex: 3;
            background: #333;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.5);
            position: sticky; top: 20px; /* 滚动时吸顶 */
        }

        /* 标题信息 */
        h1 { margin: 0; font-size: 28px; color: #fff; text-align: center;}
        h3 { color: #aaa; font-weight: normal; margin-top: 10px; text-align: center;}
        .uploader-info { font-size: 12px; color: #666; margin-top: 5px; background: #222; display: inline-block; padding: 2px 8px; border-radius: 4px; display: block; width: fit-content; margin: 10px auto;}
        .uploader-info a { color: #007bff; text-decoration: none; }

        /* APlayer & 弹幕层 */
        #aplayer { margin-top: 30px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); border-radius: 6px; z-index: 10; position: relative;}
        #danmaku-container { position: absolute; top: 100px; left: 0; width: 100%; height: 250px; pointer-events: none; overflow: hidden; z-index: 999; }
        .danmaku-item { position: absolute; color: white; font-size: 24px; font-weight: bold; text-shadow: 2px 2px 4px #000; white-space: nowrap; animation: move 8s linear forwards; font-family: "SimHei"; }
        @keyframes move { from { left: 100%; } to { left: -100%; } }

        /* 输入框与发送按钮 */
        .dm-input-box { margin-top: 15px; display: flex; gap: 10px; justify-content: center; }
        .dm-input-box input { flex: 1; padding: 10px; border-radius: 4px; border: 1px solid #555; background: #444; color: white; outline: none;}
        .dm-input-box input:focus { border-color: #007bff; }
        .btn-send { background: #ff0055; color: white; border: none; padding: 0 20px; border-radius: 4px; cursor: pointer; font-weight: bold; transition: 0.2s; }
        .btn-send:hover { background: #d60045; transform: scale(1.05); }

        /* 操作按钮区 */
        .action-bar { text-align:center; margin-top:30px; display: flex; justify-content: center; gap: 15px; }
        .btn-action { display: inline-block; color: #fff; text-decoration: none; border: 1px solid transparent; padding: 8px 20px; border-radius: 20px; font-size: 14px; cursor: pointer;}
        .btn-back { border-color: #555; color: #aaa; }
        .btn-back:hover { background: white; color: black; }
        .btn-download { background: #28a745; border-color: #28a745; }
        .btn-download:hover { background: #218838; }
        .btn-share { background: #6f42c1; border-color: #6f42c1; }
        .btn-share:hover { background: #5a32a3; }

        /* 评论区样式 */
        hr { border: 0; border-top: 1px solid #444; margin: 30px 0; }
        .comment-header { font-size: 18px; margin-bottom: 15px; border-left: 4px solid #007bff; padding-left: 10px; }
        .comment-form textarea { width: 100%; padding: 10px; border-radius: 5px; background: #eee; color: #333; resize: vertical;}
        .comment-form button { margin-top: 10px; padding: 8px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; float: right; }
        .comment-list { margin-top: 50px; clear: both; }
        .comment-item { border-bottom: 1px solid #444; padding: 15px 0; }

        /* 评论人名字 */
        .comment-user { color: #007bff; font-weight: bold; font-size: 14px; text-decoration: none; cursor: pointer; }
        .comment-user:hover { text-decoration: underline; color: #66b0ff; }
        .user-unnamed { color: #aaa; font-style: italic; font-weight: normal; }

        .comment-time { float: right; color: #666; font-size: 12px; }
        .comment-content { margin-top: 8px; font-size: 14px; color: #ddd; line-height: 1.5; }
    </style>
</head>
<body>
<% Music m = (Music)request.getAttribute("m"); if(m != null) { %>

<div class="container">
    <div class="player-left">
        <div class="player-card">
            <h1><%= m.getTitle() %></h1>
            <h3><%= m.getArtist() %></h3>
            <div class="uploader-info">
                UP主: <a href="profile?username=<%= m.getUploaderName() %>"><%= m.getUploaderNickname() %></a>
                <a href="chatPage?username=<%= m.getUploaderName() %>&targetId=0" style="color:#ffcc00; margin-left:5px; text-decoration:none;" title="发私信">💬</a>
            </div>

            <div id="danmaku-container"></div>
            <div id="aplayer"></div>

            <div class="dm-input-box">
                <input type="text" id="dmText" placeholder="🚀 发条弹幕互动一下 (回车发送)" maxlength="50" />
                <button class="btn-send" onclick="sendDanmaku()">发射</button>
            </div>

            <div class="action-bar">
                <a href="index" class="btn-action btn-back">← 返回列表</a>
                <a href="download?id=<%= m.getId() %>" class="btn-action btn-download">⬇️ 下载</a>
                <button onclick="copyShareLink()" class="btn-action btn-share">🔗 分享</button>
            </div>

            <hr>

            <div class="comment-section">
                <div class="comment-header">📝 听友评论 (<%= m.getPlayCount() %> 播放)</div>

                <% User user = (User)session.getAttribute("user"); if(user != null) { %>
                <form action="comment" method="post" class="comment-form">
                    <input type="hidden" name="musicId" value="<%= m.getId() %>">
                    <textarea name="content" rows="3" placeholder="写下你的听歌感受..." required></textarea>
                    <button type="submit">发表评论</button>
                </form>
                <% } else { %>
                <div style="text-align:center; color:#999; background:#444; padding:10px; border-radius:5px;">
                    请 <a href="login.jsp" style="color:#007bff;">登录</a> 后发表评论
                </div>
                <% } %>

                <div class="comment-list">
                    <%
                        List<Comment> comments = (List<Comment>)request.getAttribute("commentList");
                        if(comments != null && comments.size() > 0) {
                            for(Comment c : comments) {
                                // 判断显示昵称还是未命名
                                String displayName = "未命名用户";
                                String nameClass = "user-unnamed";

                                if (c.getNickname() != null && !c.getNickname().trim().isEmpty()) {
                                    displayName = c.getNickname();
                                    nameClass = "";
                                }
                    %>
                    <div class="comment-item">
                        <div>
                            <a href="profile?username=<%= c.getUsername() %>" class="comment-user <%= nameClass %>" title="ID: <%= c.getUsername() %>">
                                <%= displayName %>
                            </a>
                            <span class="comment-time"><%= c.getCreateTime() %></span>
                        </div>
                        <div class="comment-content"><%= c.getContent() %></div>
                    </div>
                    <% }} else { %>
                    <div style="text-align:center; color:#555; margin-top:20px;">暂无评论，快来抢沙发~</div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>

    <div class="recommend-box">
        <h4 style="color: #fff; margin-top: 0; border-bottom: 1px solid #555; padding-bottom: 10px;">🎲 猜你喜欢</h4>
        <%
            List<Music> recList = (List<Music>)request.getAttribute("recommendList");
            if(recList != null) {
                for(Music rm : recList) {
        %>
        <div style="display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 13px;">
            <div style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 120px;">
                <a href="play?id=<%= rm.getId() %>" style="color: #eee; text-decoration: none; font-weight: bold;"><%= rm.getTitle() %></a>
                <div style="color: #888; font-size: 12px; margin-top:2px;">UP: <%= rm.getUploaderName() %></div>
            </div>
            <div style="text-align: right;">
                <div style="color: #aaa;"><%= rm.getDuration() %></div>
                <div style="color: #666; font-size: 12px;">▶ <%= rm.getPlayCount() %></div>
            </div>
        </div>
        <% }} %>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/aplayer/1.10.1/APlayer.min.js"></script>
<script>
    var musicId = "<%= m.getId() %>";
    var contextPath = "<%= request.getContextPath() %>";

    // 1. 初始化 APlayer
    const ap = new APlayer({
        container: document.getElementById('aplayer'),
        theme: '#ff0055', autoplay: true, loop: 'all', volume: 0.7,
        audio: [{ name: '<%= m.getTitle() %>', artist: '<%= m.getArtist() %>', url: contextPath + '/<%= m.getFilePath() %>', cover: 'https://p1.music.126.net/K1p6H9l-b8r4xX8f_x8u4A==/109951165792942202.jpg?param=300x300' }]
    });

    // 2. 弹幕逻辑
    var danmakuData = [];
    fetch("danmakuList?musicId=" + musicId).then(res => res.json()).then(data => { danmakuData = data; });
    var lastTime = 0;
    ap.on('timeupdate', function () {
        var currentTime = ap.audio.currentTime;
        danmakuData.forEach(function(d) {
            if (d.videoTime > lastTime && d.videoTime <= currentTime) showDanmaku(d.content);
        });
        lastTime = currentTime;
    });
    ap.on('seeked', function () { lastTime = ap.audio.currentTime; });

    // 3. WebSocket (已修复：自动适配 https 协议)
    var wsProtocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    var wsUrl = wsProtocol + window.location.host + contextPath + "/danmaku/" + musicId;

    var ws = null;
    try {
        ws = new WebSocket(wsUrl);
        ws.onmessage = function(event) {
            var data = JSON.parse(event.data);
            // 只有当时间差很小时(实时发送)，才显示弹幕，防止历史弹幕和实时广播重复显示
            if (Math.abs(data.time - ap.audio.currentTime) < 2) showDanmaku(data.text, true);
            danmakuData.push({content: data.text, videoTime: data.time});
        };
    } catch (e) { console.error(e); }

    // 4. 发送弹幕 (修复双重显示：只发不画，等广播)
    function sendDanmaku() {
        var input = document.getElementById("dmText");
        var text = input.value.trim();
        if(text && ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ text: text, time: ap.audio.currentTime }));
            input.value = "";
        } else { alert("连接断开"); }
    }
    document.getElementById("dmText").addEventListener("keypress", function(e){ if(e.key === 'Enter') sendDanmaku(); });

    function showDanmaku(text, isSelf) {
        var container = document.getElementById("danmaku-container");
        var span = document.createElement("span");
        span.className = "danmaku-item";
        span.innerText = text;
        span.style.top = Math.floor(Math.random() * 70 + 10) + "%";
        if (isSelf) { span.style.border = "2px solid yellow"; span.style.zIndex = 10000; }
        else { span.style.color = ["#fff", "#ffe600", "#00ff00", "#00ffff"][Math.floor(Math.random() * 4)]; }
        container.appendChild(span);
        setTimeout(function() { span.remove(); }, 8000);
    }

    // 5. 分享功能
    function copyShareLink() {
        navigator.clipboard.writeText(window.location.href).then(() => alert("✅ 链接已复制！")).catch(() => alert("复制失败"));
    }
</script>

<% } else { %>
<div style="text-align:center; margin-top:100px;"><h2>🚫 未找到该音乐</h2><a href="index">返回首页</a></div>
<% } %>

<jsp:include page="chatbot.jsp" />
</body>
</html>