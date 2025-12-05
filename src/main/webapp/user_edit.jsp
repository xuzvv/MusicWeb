<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/5
  Time: 22:29
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.music.bean.User" %>
<!DOCTYPE html>
<html>
<head>
    <title>编辑资料</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; background: #f4f6f9; display: flex; justify-content: center; padding-top: 50px; }
        .box { width: 500px; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 10px 20px rgba(0,0,0,0.05); }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; color: #666; }
        input, textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
        button { width: 100%; padding: 12px; background: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; font-size:16px; }
    </style>
</head>
<body>
<% User user = (User)session.getAttribute("user"); if(user==null) response.sendRedirect("login.jsp"); %>

<div class="box">
    <h2 style="text-align:center;">✏️ 编辑个人资料</h2>
    <form action="updateUser" method="post">
        <div class="form-group">
            <label>昵称</label>
            <input type="text" name="nickname" value="<%= user.getNickname()!=null?user.getNickname():"" %>" required>
        </div>
        <div class="form-group">
            <label>个性签名 (Bio)</label>
            <textarea name="bio" rows="3"><%= user.getBio()!=null?user.getBio():"" %></textarea>
        </div>
        <div class="form-group">
            <label>头像链接 (URL, 推荐使用在线图床)</label>
            <input type="text" name="avatar" value="<%= user.getAvatar() %>" placeholder="http://...">
        </div>
        <div class="form-group">
            <label>社交主页链接</label>
            <input type="text" name="socialLink" value="<%= user.getSocialLink()!=null?user.getSocialLink():"" %>" placeholder="https://weibo.com/...">
        </div>
        <button type="submit">保存修改</button>
    </form>
    <div style="text-align:center; margin-top:15px;">
        <a href="profile?username=<%= user.getUsername() %>" style="color:#666; text-decoration:none;">取消并返回</a>
    </div>
</div>
</body>
</html>
