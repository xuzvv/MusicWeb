package com.music.controller;

import com.music.bean.User;
import com.music.dao.MusicDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {
    private MusicDao musicDao = new MusicDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) return;

        try {
            int musicId = Integer.parseInt(req.getParameter("musicId"));
            int type = Integer.parseInt(req.getParameter("type")); // 1:赞, -1:踩

            // 调用显性更新方法
            musicDao.updateUserPreferenceDirectly(user.getId(), musicId, type);

            resp.getWriter().write("success");
        } catch (Exception e) { e.printStackTrace(); }
    }
}