package com.music.controller;

import com.music.bean.User;
import com.music.service.MusicService;
import com.music.dao.InviteDao; // 引入
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private MusicService service = new MusicService();
    private InviteDao inviteDao = new InviteDao(); // 实例化

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null || !"admin".equals(user.getRole())) {
            resp.sendRedirect("index");
            return;
        }

        String action = req.getParameter("action");

        // --- 音乐管理逻辑 ---
        if ("approve".equals(action)) {
            service.approve(Integer.parseInt(req.getParameter("id")));
            resp.sendRedirect("admin?action=list");
        } else if ("delete".equals(action)) {
            service.delete(Integer.parseInt(req.getParameter("id")));
            resp.sendRedirect("admin?action=list");

            // --- ✨✨✨ 新增：邀请码管理逻辑 ✨✨✨ ---
        } else if ("addCode".equals(action)) {
            String code = req.getParameter("code");
            if(code != null && !code.trim().isEmpty()){
                inviteDao.addCode(code);
            }
            resp.sendRedirect("admin?action=list");
        } else if ("deleteCode".equals(action)) {
            inviteDao.deleteCode(Integer.parseInt(req.getParameter("id")));
            resp.sendRedirect("admin?action=list");

        } else {
            // 默认展示页面
            req.setAttribute("pendingList", service.getPendingList());
            req.setAttribute("allList", service.getAllList());
            // ✨ 注入邀请码列表
            req.setAttribute("codeList", inviteDao.getAllCodes());

            req.getRequestDispatcher("admin.jsp").forward(req, resp);
        }
    }
}