package com.music.controller;

import com.music.bean.User;
import com.music.dao.MessageDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/messageList")
public class MessageListServlet extends HttpServlet {
    private MessageDao messageDao = new MessageDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // 获取我的联系人列表
        List<Map<String, Object>> contactList = messageDao.getRecentContacts(user.getId());

        req.setAttribute("contactList", contactList);
        req.getRequestDispatcher("message_list.jsp").forward(req, resp);
    }
}