<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/24
  Time: 10:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="com.music.dao.MusicDao" %>
<%@ page import="com.music.util.DBUtil" %>
<%@ page import="java.sql.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>生成测试数据</title>
    <link rel="stylesheet" href="https://cdn.staticfile.org/twitter-bootstrap/4.3.1/css/bootstrap.min.css">
</head>
<body class="container mt-5">
<h2>🛠️ 排名算法验证数据生成器</h2>
<hr>
<%
    // 定义 5 个测试用例 (Name, Play, Sel, TotalPref)
    // 对应 Python 仿真中的 5 种场景
    Object[][] testCases = {
            {"[Test] Classic Hit (金曲)", 5000, 1000, 4000.0},  // 预期第 1
            {"[Test] Hidden Gem (冷门神曲)", 10, 5, 8.0},        // 预期第 2 (新算法应排前面)
            {"[Test] Average New (普通新歌)", 10, 2, 1.0},       // 预期第 3
            {"[Test] Mediocre Old (平庸老歌)", 10000, 2000, 100.0}, // 预期第 4 (旧算法它会排第2，新算法应排后面)
            {"[Test] Trash Hot (热门垃圾)", 5000, 1000, -500.0}   // 预期第 5
    };

    MusicDao dao = new MusicDao();

    try (Connection conn = DBUtil.getConn()) {
        // 1. 清理旧数据 (防止重复点击生成)
        out.println("<div class='alert alert-warning'>正在清理旧数据...</div>");
        PreparedStatement psDel = conn.prepareStatement("DELETE FROM music WHERE title LIKE '[Test]%'");
        psDel.executeUpdate();

        out.println("<div class='list-group'>");

        // 2. 循环插入数据
        for (Object[] row : testCases) {
            String title = (String) row[0];
            int play = (Integer) row[1];
            int sel = (Integer) row[2];
            double pref = (Double) row[3];

            // A. 插入 Music 表
            String sqlMusic = "INSERT INTO music (title, artist, uploader_name, status, file_path, duration) VALUES (?, 'System', 'admin', 1, 'test.mp3', '03:00')";
            PreparedStatement ps1 = conn.prepareStatement(sqlMusic, Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1, title);
            ps1.executeUpdate();
            ResultSet rs = ps1.getGeneratedKeys();

            if (rs.next()) {
                int musicId = rs.getInt(1);

                // B. 强制更新 播放量 和 点击量
                String sqlUpdate = "UPDATE music SET play_count=?, selection_count=? WHERE id=?";
                PreparedStatement ps2 = conn.prepareStatement(sqlUpdate);
                ps2.setInt(1, play);
                ps2.setInt(2, sel);
                ps2.setInt(3, musicId);
                ps2.executeUpdate();

                // C. 插入虚拟评分 (模拟大家一共投了这么多分)
                String sqlPref = "INSERT INTO music_preference (user_id, music_id, preference_value, is_explicit) VALUES (1, ?, ?, 0)";
                PreparedStatement ps3 = conn.prepareStatement(sqlPref);
                ps3.setInt(1, musicId);
                ps3.setDouble(2, pref);
                ps3.executeUpdate();

                // D. 🟢 关键步骤：调用 Java 算法更新分数 (这就是我们刚刚改为 Public 的方法)
                dao.updateMusicTotalPreference(musicId);

                // E. 查询算出来的分数用于展示
                String sqlCheck = "SELECT recommendation_score FROM music WHERE id=?";
                PreparedStatement ps4 = conn.prepareStatement(sqlCheck);
                ps4.setInt(1, musicId);
                ResultSet rsCheck = ps4.executeQuery();
                double score = 0;
                if(rsCheck.next()) score = rsCheck.getDouble(1);

                out.println(String.format("<div class='list-group-item'>✅ 已生成: <b>%s</b> | 播放: %d | 总评: %.1f | <span class='text-success font-weight-bold'>算出得分: %.4f</span></div>",
                        title, play, pref, score));
            }
        }
        out.println("</div>");
        out.println("<div class='alert alert-success mt-3'>所有数据生成完毕！请去主页查看排名。</div>");
        out.println("<a href='index.jsp' class='btn btn-primary'>前往主页验证排名 -></a>");

    } catch (Exception e) {
        e.printStackTrace(new java.io.PrintWriter(out));
    }
%>
</body>
</html>