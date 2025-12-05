package com.music.controller;

import com.music.dao.MusicDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/index")
public class HomeServlet extends HttpServlet {
    private MusicDao dao = new MusicDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // 1. 获取参数
        String tab = req.getParameter("tab");
        if (tab == null || tab.isEmpty()) tab = "hot"; // 默认热门

        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.isEmpty()) {
            try { page = Integer.parseInt(pageStr); } catch (Exception e) {}
        }

        int pageSize = 10; // 每页显示10条

        // 2. 查询数据
        req.setAttribute("list", dao.getMusicList(tab, page, pageSize));

        // 3. 计算分页信息
        int totalCount = dao.getMusicCount();
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);
        if (totalPage == 0) totalPage = 1;

        req.setAttribute("currTab", tab);
        req.setAttribute("currPage", page);
        req.setAttribute("totalPage", totalPage);

        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}