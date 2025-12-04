<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.Music" %>
<%@ page import="com.music.bean.User" %>
<%@ page import="com.music.bean.Comment" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>正在播放</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/aplayer/1.10.1/APlayer.min.css">

    <style>
        body { background: #222; color: #fff; font-family: "Microsoft YaHei", sans-serif; margin: 0; padding-top: 50px; }
        .container { width: 700px; margin: 0 auto; } /* 稍微加宽一点 */

        /* 播放器主体卡片 */
        .player-card { background: #333; padding: 40px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }
        h1 { margin: 0; font-size: 28px; color: #fff; text-align: center;}
        h3 { color: #aaa; font-weight: normal; margin-top: 10px; text-align: center;}
        .uploader-info { font-size: 12px; color: #666; margin-top: 5px; background: #222; display: inline-block; padding: 2px 8px; border-radius: 4px; display: block; width: fit-content; margin: 10px auto;}

        /* APlayer 容器样式微调 */
        #aplayer { margin-top: 30px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); border-radius: 6px; }

        .back-btn { display: inline-block; margin-top: 20px; color: #aaa; text-decoration: none; border: 1px solid #555; padding: 6px 15px; border-radius: 20px; transition:0.3s; font-size: 14px; text-align: center;}
        .back-btn:hover { background: white; color: black; }

        /* 分割线 */
        hr { border: 0; border-top: 1px solid #444; margin: 30px 0; }

        /* 评论区样式保持不变 */
        .comment-section { text-align: left; }
        .comment-header { font-size: 18px; margin-bottom: 15px; border-left: 4px solid #007bff; padding-left: 10px; }
        .comment-form textarea { width: 100%; padding: 10px; border-radius: 5px; border: none; resize: vertical; box-sizing: border-box; font-family: inherit;}
        .comment-form button { margin-top: 10px; padding: 8px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; float: right; }
        .comment-form button:hover { background: #0056b3; }
        .login-tip { background: #444; padding: 15px; border-radius: 5px; text-align: center; color: #ccc; }
        .login-tip a { color: #007bff; text-decoration: none; }
        .comment-list { margin-top: 50px; clear: both; }
        .comment-item { border-bottom: 1px solid #444; padding: 15px 0; }
        .comment-user { color: #007bff; font-weight: bold; font-size: 14px; }
        .comment-time { float: right; color: #666; font-size: 12px; }
        .comment-content { margin-top: 8px; font-size: 14px; color: #ddd; line-height: 1.5; }
        .no-comment { text-align: center; color: #555; margin-top: 20px; }
    </style>
</head>
<body>
<%
    Music m = (Music)request.getAttribute("m");
    if(m != null) {
%>
<div class="container">
    <div class="player-card">
        <h1><%= m.getTitle() %></h1>
        <h3><%= m.getArtist() %></h3>
        <div class="uploader-info">UP主: <%= m.getUploaderName() == null ? "未知" : m.getUploaderName() %></div>

        <div id="aplayer"></div>

        <div style="text-align:center; margin-top:20px;">
            <a href="index" class="back-btn">← 返回列表</a>
        </div>

        <hr>

        <div class="comment-section">
            <div class="comment-header">💬 听友评论 (<%= m.getPlayCount() %> 次播放)</div>

            <%
                User user = (User)session.getAttribute("user");
                if(user != null) {
            %>
            <form action="comment" method="post" class="comment-form">
                <input type="hidden" name="musicId" value="<%= m.getId() %>">
                <textarea name="content" rows="3" placeholder="写下你的听歌感受..." required></textarea>
                <button type="submit">发表评论</button>
            </form>
            <% } else { %>
            <div class="login-tip">需要 <a href="login.jsp">登录</a> 后才能发表评论</div>
            <% } %>

            <div class="comment-list">
                <%
                    List<Comment> comments = (List<Comment>)request.getAttribute("commentList");
                    if(comments != null && comments.size() > 0) {
                        for(Comment c : comments) {
                %>
                <div class="comment-item">
                    <div>
                        <span class="comment-user"><%= c.getUsername() %></span>
                        <span class="comment-time"><%= c.getCreateTime() %></span>
                    </div>
                    <div class="comment-content"><%= c.getContent() %></div>
                </div>
                <% }} else { %>
                <div class="no-comment">暂无评论，快来抢沙发吧~</div>
                <% } %>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/aplayer/1.10.1/APlayer.min.js"></script>

<script>
    const ap = new APlayer({
        container: document.getElementById('aplayer'), // 绑定到上面的 div
        theme: '#007bff',  // 主题色 (蓝色)
        autoplay: true,    // 自动播放
        loop: 'all',       // 循环播放
        volume: 0.7,       // 默认音量
        listFolded: false, // 列表默认展开
        audio: [{
            name: '<%= m.getTitle() %>',   // 歌名
            artist: '<%= m.getArtist() %>', // 歌手
            // 音乐文件地址
            url: '<%= request.getContextPath() %>/<%= m.getFilePath() %>',
            // 封面图片 (我找了一个通用的黑胶唱片图作为默认封面)
            cover: 'https://p1.music.126.net/K1p6H9l-b8r4xX8f_x8u4A==/109951165792942202.jpg?param=300x300'
        }]
    });
</script>

<% } else { %>
<div style="text-align:center; color:white; margin-top:100px;">
    <h2>🚫 未找到该音乐信息</h2>
    <a href="index" style="color:#007bff;">返回首页</a>
</div>
<% } %>
</body>
</html>