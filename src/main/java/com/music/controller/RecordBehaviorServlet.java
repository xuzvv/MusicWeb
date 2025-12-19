package com.music.controller;

import com.music.bean.User;
import com.music.dao.MusicDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/recordBehavior")
@MultipartConfig
public class RecordBehaviorServlet extends HttpServlet {
    private MusicDao musicDao = new MusicDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) return;

        try {
            int musicId = Integer.parseInt(req.getParameter("musicId"));
            // 前端 JS 传来的是秒 (double)
            double playTimeDouble = Double.parseDouble(req.getParameter("playTime"));
            double totalTimeDouble = Double.parseDouble(req.getParameter("totalTime"));

            int playTime = (int) playTimeDouble;
            int totalTime = (int) totalTimeDouble;

            // 调用 DAO (内部已含 >0.9 -> 1.0 的逻辑)
            musicDao.updateUserPreference(user.getId(), musicId, playTime, totalTime);

            resp.getWriter().write("success");
        } catch (Exception e) { e.printStackTrace(); }
    }
}