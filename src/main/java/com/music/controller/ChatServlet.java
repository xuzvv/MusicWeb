package com.music.controller;

import com.music.bean.Message;
import com.music.bean.User;
import com.music.dao.MessageDao;
import com.music.dao.UserDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/chatPage")
public class ChatServlet extends HttpServlet {
    private MessageDao messageDao = new MessageDao();
    private UserDao userDao = new UserDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        User targetUser = null;

        // 优先通过 ID 查找目标
        String targetIdStr = req.getParameter("targetId");
        if (targetIdStr != null && !"0".equals(targetIdStr)) {
            // 这里为了简单，我们还是通过 username 查，或者你可以给 UserDao 加一个 getUserById
            // 假设 targetId 是准确的，但我们现在只有 getUserByUsername 方法
            // 所以我们优先用 username 参数
        }

        String username = req.getParameter("username");
        if (username != null) {
            targetUser = userDao.getUserByUsername(username);
        }

        if (targetUser == null) {
            resp.sendRedirect("index");
            return;
        }

        // 1. 获取历史记录
        List<Message> history = messageDao.getChatHistory(currentUser.getId(), targetUser.getId());

        // 2. 标记对方发给我的消息为已读
        messageDao.markAsRead(targetUser.getId(), currentUser.getId());

        req.setAttribute("targetUser", targetUser);
        req.setAttribute("history", history);
        req.getRequestDispatcher("chat.jsp").forward(req, resp);
    }
}