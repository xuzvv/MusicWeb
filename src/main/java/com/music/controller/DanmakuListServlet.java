package com.music.controller;

import com.google.gson.Gson;
import com.music.dao.DanmakuDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/danmakuList")
public class DanmakuListServlet extends HttpServlet {
    private DanmakuDao dao = new DanmakuDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String musicId = req.getParameter("musicId");

        // 返回 JSON 列表
        String json = new Gson().toJson(dao.getByMusicId(Integer.parseInt(musicId)));
        resp.getWriter().write(json);
    }
}