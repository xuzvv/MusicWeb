<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/4
  Time: 23:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.music.bean.*" %>
<html>
<head><title>管理后台</title></head>
<body>
<div style="width:800px; margin:20px auto;">
    <h2>🛠️ 管理员控制台 <a href="index" style="font-size:14px;">返回首页</a></h2>

    <div style="margin: 20px 0; text-align: right;">
        <a href="dashboard.jsp" style="background: #6610f2; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;">
            📊 查看数据可视化大屏
        </a>
    </div>
    <h3>待审核列表</h3>
    <table border="1" width="100%">
        <tr><th>歌名</th><th>上传者</th><th>操作</th></tr>
        <% List<Music> pending = (List<Music>)request.getAttribute("pendingList");
            if(pending != null) for(Music m : pending) { %>
        <tr>
            <td><%=m.getTitle()%></td>
            <td><%=m.getUploaderName()%></td>
            <td>
                <a href="admin?action=approve&id=<%=m.getId()%>">✅ 通过</a> |
                <a href="admin?action=delete&id=<%=m.getId()%>" onclick="return confirm('确认删除？')">❌ 删除</a>
            </td>
        </tr>
        <% } %>
    </table>

    <h3>所有音乐管理</h3>
    <table border="1" width="100%">
        <tr><th>ID</th><th>歌名</th><th>状态</th><th>操作</th></tr>
        <% List<Music> all = (List<Music>)request.getAttribute("allList");
            if(all != null) for(Music m : all) { %>
        <tr>
            <td><%=m.getId()%></td>
            <td><%=m.getTitle()%></td>
            <td><%= m.getStatus()==1 ? "正常" : "待审" %></td>
            <td><a href="admin?action=delete&id=<%=m.getId()%>" style="color:red;">删除</a></td>
        </tr>
        <% } %>
    </table>
</div>
</body>
</html>