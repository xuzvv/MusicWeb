package com.music.controller;

import com.music.bean.Music;
import com.music.bean.User; // 引入User
import com.music.dao.MusicDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Collections;

@WebServlet("/index")
public class HomeServlet extends HttpServlet {
    private MusicDao dao = new MusicDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.isEmpty()) {
            try { page = Integer.parseInt(pageStr); } catch (Exception e) {}
        }
        int pageSize = 10;
        String keyword = req.getParameter("keyword");

        if (keyword != null && !keyword.trim().isEmpty()) {
            // ... (搜索逻辑保持不变) ...
            keyword = keyword.trim();
            req.setAttribute("list", dao.searchMusic(keyword, page, pageSize));
            int totalCount = dao.getSearchCount(keyword);
            int totalPage = (int) Math.ceil((double) totalCount / pageSize);
            if (totalPage == 0) totalPage = 1;
            req.setAttribute("isSearch", true);
            req.setAttribute("keyword", keyword);
            req.setAttribute("totalPage", totalPage);
        } else {
            String tab = req.getParameter("tab");
            if (tab == null || tab.isEmpty()) tab = "hot"; // 默认还是 hot

            if ("random".equals(tab)) {
                // ✨✨✨ 修改：如果是 "猜你喜欢" (random)，应用新的推荐算法 ✨✨✨
                User user = (User) req.getSession().getAttribute("user");
                List<Music> fullRecList;

                if (user != null) {
                    // 登录用户：Algorithm 1 + 2 + 3 混合排序
                    fullRecList = dao.getRecommendationForUser(user.getId());
                } else {
                    // 游客：Algorithm 3 (Selection Count) 排序
                    fullRecList = dao.getRecommendationForGuest();
                }

                // --- 内存分页逻辑 ---
                int totalCount = fullRecList.size();
                int totalPage = (int) Math.ceil((double) totalCount / pageSize);
                if (totalPage == 0) totalPage = 1;

                // 防止页码越界
                if (page > totalPage) page = totalPage;
                if (page < 1) page = 1;

                int fromIndex = (page - 1) * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, totalCount);

                List<Music> pageList;
                if (fromIndex >= totalCount) {
                    pageList = Collections.emptyList();
                } else {
                    pageList = fullRecList.subList(fromIndex, toIndex);
                }

                req.setAttribute("list", pageList);
                req.setAttribute("currTab", tab);
                req.setAttribute("totalPage", totalPage);

            } else {
                // 其他榜单 (hot, new) 使用原有 SQL 分页逻辑
                req.setAttribute("list", dao.getMusicList(tab, page, pageSize));
                int totalCount = dao.getMusicCount();
                int totalPage = (int) Math.ceil((double) totalCount / pageSize);
                if (totalPage == 0) totalPage = 1;
                req.setAttribute("currTab", tab);
                req.setAttribute("totalPage", totalPage);
            }
        }

        req.setAttribute("currPage", page);
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}