package com.music.controller;

import com.music.bean.Music;
import com.music.bean.User;
import com.music.dao.MusicDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet("/index")
public class HomeServlet extends HttpServlet {
    private MusicDao dao = new MusicDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // 1. 获取并处理页码 (默认为 1)
        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (Exception e) {
                page = 1;
            }
        }
        int pageSize = 10; // 每页显示 10 条

        // 2. 处理搜索逻辑 (Search Logic)
        String keyword = req.getParameter("keyword");
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = keyword.trim();
            // 搜索数据库
            List<Music> list = dao.searchMusic(keyword, page, pageSize);
            // 计算搜索结果总页数
            int totalCount = dao.getSearchCount(keyword);
            int totalPage = (int) Math.ceil((double) totalCount / pageSize);
            if (totalPage == 0) totalPage = 1;

            // 设置属性并转发
            req.setAttribute("list", list);
            req.setAttribute("isSearch", true);
            req.setAttribute("keyword", keyword);
            req.setAttribute("totalPage", totalPage);
            req.setAttribute("currPage", page);

            req.getRequestDispatcher("/index.jsp").forward(req, resp);
            return; // 搜索模式下，后续的 Tab 逻辑不再执行
        }

        // 3. 处理 Tab 标签页 (hot / new / random)
        String tab = req.getParameter("tab");
        if (tab == null || tab.isEmpty()) {
            tab = "hot"; // 默认显示最热
        }

        // ✨✨✨ 核心修改：接入新推荐算法 (针对 'random' 猜你喜欢) ✨✨✨
        if ("random".equals(tab)) {
            // 获取当前登录用户
            User user = (User) req.getSession().getAttribute("user");
            List<Music> fullRecList;

            if (user != null) {
                // 🟢 登录用户：调用 5(喜好) + 4(习惯) + 补位 混合推荐
                fullRecList = dao.getRecommendationForUser(user.getId());
            } else {
                // 🔴 游客：调用全站综合热度推荐
                fullRecList = dao.getRecommendationForGuest();
            }

            // --- 内存分页逻辑 (Memory Pagination) ---
            // 因为 dao.getRecommendation... 返回的是完整列表(Top 20/50)，我们需要手动截取当前页的数据
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
            req.setAttribute("totalPage", totalPage);

        } else {
            // 4. 常规榜单 (hot / new)
            // 这些依然走数据库层面的分页 (LIMIT ?, ?)
            List<Music> list = dao.getMusicList(tab, page, pageSize);

            int totalCount = dao.getMusicCount(); // 全站有效歌曲数
            int totalPage = (int) Math.ceil((double) totalCount / pageSize);
            if (totalPage == 0) totalPage = 1;

            req.setAttribute("list", list);
            req.setAttribute("totalPage", totalPage);
        }

        // 5. 设置公共属性
        req.setAttribute("currTab", tab);
        req.setAttribute("currPage", page);

        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}