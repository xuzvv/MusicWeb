package com.music.controller;

import com.music.dao.MusicDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/algo-demo")
public class AlgoDemoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // GET 请求转发到 JSP 演示页面
        req.getRequestDispatcher("algo_lab.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // POST 请求处理实时计算
        req.setCharacterEncoding("UTF-8");

        try {
            int playTime = Integer.parseInt(req.getParameter("playTime"));
            int totalTime = Integer.parseInt(req.getParameter("totalTime"));

            // 分别计算新旧算法得分
            // 这里调用 MusicDao 中的静态方法，保证演示结果与真实业务逻辑一致
            double oldScore = MusicDao.calculateOldScore(playTime, totalTime);
            double aiScore = MusicDao.calculateAIScore(playTime, totalTime);

            // 构造简单 JSON 返回
            resp.setContentType("application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            String json = String.format("{\"oldScore\": %.4f, \"aiScore\": %.4f}", oldScore, aiScore);
            out.print(json);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(400, "Invalid Parameters");
        }
    }
}