<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/5
  Time: 13:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>创作手记 - X² Voice</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; background: #f9f9f9; padding: 40px; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 15px rgba(0,0,0,0.05); }

        .header { border-bottom: 1px solid #eee; padding-bottom: 20px; margin-bottom: 30px; }
        h1 { margin: 0 0 10px 0; color: #333; }
        .meta { color: #888; font-size: 14px; }

        .content { font-size: 16px; line-height: 1.8; color: #444; min-height: 200px; white-space: pre-wrap; /* 保留换行格式 */ }

        .empty-state { text-align: center; color: #999; padding: 50px; background: #f5f5f5; border-radius: 8px; }

        /* 编辑器样式 */
        .editor-box { margin-top: 30px; border-top: 2px dashed #ddd; padding-top: 20px; }
        textarea { width: 100%; height: 300px; padding: 15px; border: 1px solid #ddd; border-radius: 5px; font-family: inherit; font-size: 16px; resize: vertical; }
        .btn-save { background: #007bff; color: white; border: none; padding: 10px 25px; border-radius: 5px; cursor: pointer; font-size: 16px; margin-top: 15px; }
        .btn-back { text-decoration: none; color: #666; margin-right: 15px; }
    </style>
</head>
<body>
<%
    Music m = (Music)request.getAttribute("music");
    Article a = (Article)request.getAttribute("article");
    boolean isAuthor = (Boolean)request.getAttribute("isAuthor");
%>

<div class="container">
    <div class="header">
        <a href="index" class="btn-back">← 返回首页</a>
        <a href="play?id=<%= m.getId() %>" class="btn-back">▶ 去听歌</a>

        <h1 style="margin-top: 20px;">📜 关于《<%= m.getTitle() %>》</h1>
        <div class="meta">
            创作者：<strong><%= m.getArtist() %></strong> (UP: <%= m.getUploaderName() %>)
            <% if(a != null) { %> | 更新于：<%= a.getUpdateTime() %> <% } %>
        </div>
    </div>

    <% if(a != null && a.getContent() != null && !a.getContent().isEmpty()) { %>
    <div class="content"><%= a.getContent() %></div>
    <% } else { %>
    <div class="empty-state">
        <h3>📭 暂无手记</h3>
        <p><%= isAuthor ? "作为创作者，快来写下这首歌背后的故事吧！" : "作者还在酝酿中..." %></p>
    </div>
    <% } %>

    <% if(isAuthor) { %>
    <div class="editor-box">
        <h3>✏️ 编辑创作手记</h3>
        <form action="article" method="post">
            <input type="hidden" name="musicId" value="<%= m.getId() %>">
            <textarea name="content" placeholder="分享你的创作灵感、使用的乐器、或者歌词背后的含义..."><%= (a != null ? a.getContent() : "") %></textarea>
            <div style="text-align: right;">
                <button type="submit" class="btn-save">发布手记</button>
            </div>
        </form>
    </div>
    <% } %>
</div>
</body>
</html>