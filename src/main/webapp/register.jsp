<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/4
  Time: 23:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>注册 - Echo</title>
    <style>
        body { font-family: sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .box { background: white; padding: 40px; border-radius: 8px; width: 350px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); text-align: center; }
        input { width: 90%; padding: 10px; margin: 10px 0; border: 1px solid #ddd; border-radius: 4px; }
        button { width: 96%; padding: 10px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; margin-top: 10px;}
    </style>
</head>
<body>
<div class="box">
    <h2>用户注册</h2>
    <p style="color:red; font-size:14px;">${msg}</p>
    <form action="auth" method="post">
        <input type="hidden" name="action" value="register">
        <input type="text" name="username" placeholder="用户名" required>
        <input type="password" name="password" placeholder="密码" required>

        <input type="text" name="inviteCode" placeholder="请输入邀请码 (如 ECHO2025)" required style="border: 2px solid #007bff;">

        <button type="submit">立即注册</button>
    </form>
    <div style="margin-top: 20px; font-size: 14px;">
        <a href="login.jsp" style="text-decoration: none; color: #007bff;">已有账号？去登录</a>
    </div>
</div>
</body>
</html>