package com.music.controller;

import com.music.bean.Music;
import com.music.dao.CommentDao;
import com.music.dao.MusicDao; // 引入DAO
import com.music.service.MusicService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/play")
public class PlayServlet extends HttpServlet {
    private MusicService service = new MusicService();
    private CommentDao commentDao = new CommentDao();
    private MusicDao musicDao = new MusicDao(); // 用于获取推荐列表

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if(idStr != null) {
            int musicId = Integer.parseInt(idStr);

            // 1. 获取音乐详情（同时增加播放次数）
            Music music = service.play(musicId);

            // 2. 获取这首歌的评论列表
            req.setAttribute("commentList", commentDao.getCommentsByMusicId(musicId));

            // 3. 【新增】获取 5 首随机推荐歌曲
            req.setAttribute("recommendList", musicDao.getRandomMusicList(5));

            // 4. 存入请求域并转发
            req.setAttribute("m", music);
            req.getRequestDispatcher("/player.jsp").forward(req, resp);
        }
    }
}