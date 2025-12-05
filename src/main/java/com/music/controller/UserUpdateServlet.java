package com.music.controller;

import com.music.bean.User;
import com.music.dao.UserDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/updateUser")
public class UserUpdateServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // 获取表单数据
        String nickname = req.getParameter("nickname");
        String bio = req.getParameter("bio");
        String socialLink = req.getParameter("socialLink");
        String avatar = req.getParameter("avatar");

        // 更新对象
        currentUser.setNickname(nickname);
        currentUser.setBio(bio);
        currentUser.setSocialLink(socialLink);
        if(avatar != null && !avatar.isEmpty()) currentUser.setAvatar(avatar);

        // 保存到数据库
        userDao.updateUser(currentUser);

        // 更新Session
        session.setAttribute("user", currentUser);

        // 回到个人主页
        resp.sendRedirect("profile?username=" + currentUser.getUsername());
    }
}