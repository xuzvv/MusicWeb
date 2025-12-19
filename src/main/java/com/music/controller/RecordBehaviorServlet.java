package com.music.controller;

import com.music.bean.User;
import com.music.dao.MusicDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/recordBehavior")
public class RecordBehaviorServlet extends HttpServlet {
    private MusicDao musicDao = new MusicDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String musicIdStr = req.getParameter("musicId");
        String playTimeStr = req.getParameter("playTime");
        String totalTimeStr = req.getParameter("totalTime");

        User user = (User) req.getSession().getAttribute("user");

        if (user != null && musicIdStr != null && playTimeStr != null && totalTimeStr != null) {
            try {
                int musicId = Integer.parseInt(musicIdStr);
                int playTime = (int) Double.parseDouble(playTimeStr);
                int totalTime = (int) Double.parseDouble(totalTimeStr);

                // 调用隐性更新方法 (内部含绝对值判断逻辑)
                musicDao.updateUserPreference(user.getId(), musicId, playTime, totalTime);

            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}