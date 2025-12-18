<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.music.bean.Music" %>
<%@ page import="com.music.bean.User" %>
<!DOCTYPE html>
<html>
<head>
    <title>X² Voice | 个人音乐分享平台</title>
    <style>
        /* 全局重置 */
        body { margin: 0; padding: 0; font-family: "Microsoft YaHei", "Segoe UI", sans-serif; background-color: #f4f6f9; color: #333; }
        a { text-decoration: none; transition: 0.3s; color: #333; }
        ul { list-style: none; padding: 0; margin: 0; }

        /* 主容器 */
        .container {
            width: 1000px;
            margin: 40px auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
            overflow: hidden;
            min-height: 800px;
            display: flex;
            flex-direction: column;
        }

        /* 顶部导航栏 */
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px 40px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: white;
        }

        /* 品牌 & 标语 */
        .brand { display: flex; flex-direction: column; }
        .brand h1 { margin: 0; font-size: 24px; font-weight: bold; letter-spacing: 1px; color: white; }
        .brand .slogan { font-size: 12px; opacity: 0.9; margin-top: 5px; font-weight: normal; letter-spacing: 2px; color: rgba(255,255,255,0.8); }

        /* 动态时钟 */
        .clock-box {
            font-family: 'Consolas', monospace;
            background: rgba(0, 0, 0, 0.2);
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 14px;
            color: #fff;
            display: flex;
            align-items: center;
            gap: 8px;
            border: 1px solid rgba(255,255,255,0.1);
        }

        /* 搜索框 */
        .search-box {
            display: flex; align-items: center;
            background: rgba(255,255,255,0.2);
            border-radius: 20px;
            padding: 5px 15px;
            border: 1px solid rgba(255,255,255,0.3);
            width: 220px;
        }
        .search-box input {
            background: transparent; border: none; outline: none;
            color: white; font-size: 13px; width: 100%;
        }
        .search-box input::placeholder { color: rgba(255,255,255,0.7); }
        .search-btn { cursor: pointer; color: white; font-size: 14px; border:none; background:transparent;}

        /* 用户面板 & GitHub按钮 */
        .user-panel { font-size: 14px; display: flex; align-items: center; gap: 15px; }
        .user-panel a { color: rgba(255,255,255,0.9); font-weight: 500; }
        .user-panel a:hover { color: white; text-decoration: underline; }

        .btn-github {
            background: #333; color: white !important;
            padding: 5px 10px; border-radius: 4px; font-size: 12px;
            display: flex; align-items: center; gap: 5px;
            border: 1px solid #555; text-decoration: none !important;
        }
        .btn-github:hover { background: black; border-color: white; }

        .btn-upload {
            background: #fff; color: #764ba2 !important;
            padding: 6px 15px; border-radius: 20px; font-weight: bold;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2); text-decoration: none !important;
        }
        .btn-upload:hover { background: #f0f0f0; }

        /* Tabs 导航条 */
        .tabs { display: flex; border-bottom: 1px solid #eee; padding: 0 20px; margin-top: 20px; }
        .tab-item { padding: 15px 25px; cursor: pointer; color: #666; font-weight: bold; border-bottom: 3px solid transparent; font-size: 16px; display: block; }
        .tab-item:hover { color: #764ba2; }
        .tab-item.active { color: #764ba2; border-bottom-color: #764ba2; }

        /* 列表区域 */
        .content-area { flex: 1; padding: 0 20px; }
        .list-item {
            padding: 15px 20px; border-bottom: 1px solid #f0f0f0;
            display: flex; justify-content: space-between; align-items: center;
        }
        .list-item:hover { background-color: #f9f9f9; transform: translateX(5px); }

        .music-info strong { font-size: 16px; color: #333; }
        .music-info .artist { color: #888; font-size: 13px; margin-left: 8px; }
        .tag-up { background-color: #17a2b8; color: white; font-size: 12px; padding: 2px 6px; border-radius: 4px; margin-left: 10px; cursor: pointer; }
        .duration { color: #999; font-size: 13px; margin-right: 15px; font-family: monospace; }

        /* 按钮 */
        .btn-play { color: #fff; background: #007bff; padding: 5px 15px; border-radius: 20px; font-size: 13px; box-shadow: 0 2px 5px rgba(0,114,255,0.3); }
        .btn-play:hover { background: #0056b3; box-shadow: 0 4px 8px rgba(0,114,255,0.4); }
        .btn-story { color: #6f42c1; background: #f3f0ff; padding: 5px 12px; border-radius: 4px; font-size: 13px; margin-right: 10px; }
        .btn-story:hover { background: #e0d4fc; }

        /* 分页条 */
        .pagination { display: flex; justify-content: center; padding: 30px; gap: 10px; align-items: center; margin-top: auto; }
        .page-link { padding: 8px 16px; border: 1px solid #ddd; border-radius: 4px; background: white; color: #666; }
        .page-link.active { background: #764ba2; color: white; border-color: #764ba2; }
        .page-link:hover:not(.active) { background: #eee; }

        /* 页脚 */
        .footer {
            border-top: 1px solid #eee;
            padding: 20px;
            text-align: center;
            font-size: 12px;
            color: #aaa;
            background: #fafafa;
        }
        .footer a { color: #aaa; }
        .footer a:hover { color: #764ba2; }

        .search-title { padding: 20px; font-size: 18px; color: #764ba2; border-bottom: 1px solid #eee; }
    </style>
</head>
<body>

<div class="container">
    <div class="header">
        <div class="brand">
            <h1>X² Voice</h1>
            <span class="slogan">听见未知的频率</span>
        </div>

        <div class="clock-box">
            <span style="font-size: 16px;">🕒</span>
            <span id="currentTime">Loading...</span>
        </div>

        <form action="index" method="get" class="search-box">
            <input type="text" name="keyword" placeholder="搜歌名 / 歌手 / UP主..." value="<%= request.getParameter("keyword")!=null?request.getParameter("keyword"):"" %>">
            <button type="submit" class="search-btn">🔍</button>
        </form>

        <div class="user-panel">
            <a href="https://github.com/TestDemoW/MusicWeb" target="_blank" class="btn-github">
                ⭐ GitHub
            </a>

            <span style="opacity: 0.3;">|</span>

            <%
                User user = (User)session.getAttribute("user");
                if(user == null) {
            %>
            <a href="login.jsp">登录</a>
            <a href="register.jsp">注册</a>
            <% } else { %>
            <a href="messageList" title="我的消息" style="font-size: 18px; text-decoration: none;">📩</a>

            <span>欢迎, <a href="profile?username=<%= user.getUsername() %>" style="font-weight:bold; text-decoration:underline;"><%= user.getNickname() != null ? user.getNickname() : user.getUsername() %></a></span>

            <a href="upload.jsp" class="btn-upload">➕ 发布</a>

            <% if("admin".equals(user.getRole())) { %>
            <a href="admin" style="color:#ffcccc; font-weight:bold;">[管理后台]</a>
            <% } %>

            <a href="auth?action=logout" style="opacity: 0.7;">退出</a>
            <% } %>
        </div>
    </div>

    <%
        String currTab = (String)request.getAttribute("currTab");
        String keyword = (String)request.getAttribute("keyword");
        Boolean isSearch = (Boolean)request.getAttribute("isSearch");
        if(isSearch == null) isSearch = false;
    %>

    <% if(isSearch) { %>
    <div class="search-title">🔍 "<strong><%= keyword %></strong>" 的搜索结果：<a href="index" style="font-size:12px; float:right;">[清除搜索]</a></div>
    <% } else { %>
    <div class="tabs">
        <a href="index?tab=hot" class="tab-item <%= "hot".equals(currTab)?"active":"" %>">🔥 热门榜单</a>
        <a href="index?tab=new" class="tab-item <%= "new".equals(currTab)?"active":"" %>">✨ 最新发布</a>
        <a href="index?tab=random" class="tab-item <%= "random".equals(currTab)?"active":"" %>">🎲 猜你喜欢</a>
    </div>
    <% } %>

    <div class="content-area">
        <% List<Music> list = (List<Music>)request.getAttribute("list");
            if(list != null && list.size() > 0) {
                for(Music m : list) { %>
        <div class="list-item">
            <div class="music-info">
                <strong><%= m.getTitle() %></strong>
                <span class="artist"> - <%= m.getArtist() %></span>
                <a href="profile?username=<%= m.getUploaderName() %>" class="tag-up" title="访问主页">UP: <%= m.getUploaderNickname() %></a>
            </div>

            <div class="actions" style="display: flex; align-items: center;">
                <span class="duration">⏱ <%= m.getDuration() %></span>
                <span style="font-size:12px; color:#999; margin-right:15px;">👂 <%= m.getPlayCount() %></span>

                <a href="article?musicId=<%= m.getId() %>" class="btn-story">📖 手记</a>

                <a href="play?id=<%= m.getId() %>" class="btn-play">▶ Play</a>
            </div>
        </div>
        <% }} else { %>
        <div style="text-align:center; padding: 60px; color: #999;">
            <h3>👻 哎呀，什么也没找到...</h3>
            <p>换个关键词试试？或者 <a href="index">返回首页</a></p>
        </div>
        <% } %>
    </div>

    <%
        int currPage = (Integer)request.getAttribute("currPage");
        int totalPage = (Integer)request.getAttribute("totalPage");
        String baseUrl = isSearch ? "index?keyword=" + keyword + "&" : "index?tab=" + currTab + "&";
    %>
    <div class="pagination">
        <% if(currPage > 1) { %>
        <a href="<%= baseUrl %>page=<%=currPage-1%>" class="page-link">上一页</a>
        <% } %>

        <span class="page-link active">第 <%= currPage %> 页 / 共 <%= totalPage %> 页</span>

        <% if(currPage < totalPage) { %>
        <a href="<%= baseUrl %>page=<%=currPage+1%>" class="page-link">下一页</a>
        <% } %>
    </div>

    <div class="footer">
        <p>&copy; 2025 x2vv.com | X² Voice Studio. All Rights Reserved.</p>
<%--        <p>--%>
<%--            <a href="https://beian.miit.gov.cn/" target="_blank">京ICP备88888888号-1</a>--%>
<%--            &nbsp;|&nbsp;--%>
<%--            <a href="#">公网安备 1101080202xxxx号</a>--%>
<%--        </p>--%>
    </div>
</div>

<script>
    function updateTime() {
        var now = new Date();
        var timeStr = now.getFullYear() + "-" +
            String(now.getMonth() + 1).padStart(2, '0') + "-" +
            String(now.getDate()).padStart(2, '0') + " " +
            String(now.getHours()).padStart(2, '0') + ":" +
            String(now.getMinutes()).padStart(2, '0') + ":" +
            String(now.getSeconds()).padStart(2, '0');

        var el = document.getElementById('currentTime');
        if(el) el.innerText = timeStr;
    }
    updateTime();
    setInterval(updateTime, 1000);
</script>

<jsp:include page="chatbot.jsp" />
</body>
</html>