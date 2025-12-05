package com.music.controller;

import com.music.bean.Music;
import com.music.bean.User;
import com.music.dao.MusicDao;
import com.music.dao.UserDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {
    private UserDao userDao = new UserDao();
    private MusicDao musicDao = new MusicDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");

        // 如果没传用户名，查看自己的
        if (username == null) {
            User currentUser = (User) req.getSession().getAttribute("user");
            if (currentUser != null) {
                username = currentUser.getUsername();
            } else {
                resp.sendRedirect("login.jsp");
                return;
            }
        }

        // 1. 获取用户信息
        User targetUser = userDao.getUserByUsername(username);
        if (targetUser == null) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write("<h3>用户不存在</h3><a href='index'>返回首页</a>");
            return;
        }

        // 2. 获取该用户发布的作品
        List<Music> musicList = musicDao.getMusicByUploader(username);

        req.setAttribute("targetUser", targetUser);
        req.setAttribute("musicList", musicList);
        req.getRequestDispatcher("user_profile.jsp").forward(req, resp);
    }
}