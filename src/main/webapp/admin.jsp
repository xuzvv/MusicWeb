<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/4
  Time: 23:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.music.bean.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>管理后台 - Echo</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; background: #f4f6f9; padding: 20px; }
        .container { width: 900px; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
        h2 { border-bottom: 2px solid #764ba2; padding-bottom: 15px; color: #333; display: flex; justify-content: space-between; align-items: center;}
        h3 { margin-top: 30px; color: #555; background: #f8f9fa; padding: 10px; border-radius: 5px; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 12px; border-bottom: 1px solid #eee; text-align: left; font-size: 14px; }
        th { background: #f1f1f1; color: #666; }
        a { text-decoration: none; color: #007bff; }
        .btn-del { color: red; }
        .btn-pass { color: green; font-weight: bold; }
        .btn-dash { background: #6610f2; color: white !important; padding: 8px 15px; border-radius: 5px; font-size: 14px; }

        /* 邀请码表单 */
        .code-form { display: flex; gap: 10px; margin-bottom: 10px; }
        .code-form input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; flex: 1; }
        .code-form button { padding: 8px 20px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; }
    </style>
</head>
<body>
<div class="container">
    <h2>
        <span>🛠️ 管理员控制台</span>
        <div>
            <a href="dashboard.jsp" class="btn-dash">📊 数据驾驶舱</a>
            <a href="index" style="font-size:14px; margin-left:15px; color:#666;">返回首页</a>
        </div>
    </h2>

    <h3>🔑 邀请码管理</h3>
    <div style="background: #fffbe6; padding: 15px; border: 1px solid #ffe58f; border-radius: 5px; margin-bottom: 15px;">
        <form action="admin" method="get" class="code-form">
            <input type="hidden" name="action" value="addCode">
            <input type="text" name="code" placeholder="输入新邀请码 (例如: VIP2025)" required>
            <button type="submit">➕ 生成邀请码</button>
        </form>
    </div>
    <table border="0">
        <tr><th>ID</th><th>邀请码</th><th>状态</th><th>操作</th></tr>
        <%
            List<Map<String, Object>> codeList = (List<Map<String, Object>>)request.getAttribute("codeList");
            if(codeList != null) {
                for(Map<String, Object> map : codeList) {
                    int isUsed = (Integer)map.get("isUsed");
        %>
        <tr>
            <td><%= map.get("id") %></td>
            <td style="font-family: monospace; font-weight: bold; color: #d63384;"><%= map.get("code") %></td>
            <td>
                <% if(isUsed == 1) { %>
                <span style="color:red; background:#ffe6e6; padding:2px 6px; border-radius:4px; font-size:12px;">已使用</span>
                <% } else { %>
                <span style="color:green; background:#e6ffed; padding:2px 6px; border-radius:4px; font-size:12px;">未使用</span>
                <% } %>
            </td>
            <td>
                <a href="admin?action=deleteCode&id=<%= map.get("id") %>" class="btn-del" onclick="return confirm('删除此码？')">删除</a>
            </td>
        </tr>
        <% }} else { %>
        <tr><td colspan="4" style="text-align:center; color:#999;">暂无数据</td></tr>
        <% } %>
    </table>

    <h3>🎵 待审核音乐</h3>
    <table border="0">
        <tr><th>歌名</th><th>上传者</th><th>时长</th><th>操作</th></tr>
        <% List<Music> pending = (List<Music>)request.getAttribute("pendingList");
            if(pending != null && pending.size() > 0) {
                for(Music m : pending) { %>
        <tr>
            <td><%=m.getTitle()%> - <%=m.getArtist()%></td>
            <td><%=m.getUploaderName()%></td>
            <td><%=m.getDuration()%></td>
            <td>
                <a href="admin?action=approve&id=<%=m.getId()%>" class="btn-pass">✅ 通过</a> |
                <a href="admin?action=delete&id=<%=m.getId()%>" class="btn-del" onclick="return confirm('确认删除？')">❌ 驳回</a>
            </td>
        </tr>
        <% }} else { %>
        <tr><td colspan="4" style="text-align:center; color:#999;">暂无待审核任务</td></tr>
        <% } %>
    </table>

    <h3>📂 所有音乐库</h3>
    <table border="0">
        <tr><th>ID</th><th>歌名</th><th>状态</th><th>操作</th></tr>
        <% List<Music> all = (List<Music>)request.getAttribute("allList");
            if(all != null) for(Music m : all) { %>
        <tr>
            <td><%=m.getId()%></td>
            <td><%=m.getTitle()%></td>
            <td><%= m.getStatus()==1 ? "<span style='color:green'>正常</span>" : "<span style='color:orange'>待审</span>" %></td>
            <td><a href="admin?action=delete&id=<%=m.getId()%>" class="btn-del">删除</a></td>
        </tr>
        <% } %>
    </table>
</div>
</body>
</html>