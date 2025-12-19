package com.music.controller;

import com.music.bean.Music;
import com.music.bean.User;
import com.music.dao.CommentDao;
import com.music.dao.MusicDao;
import com.music.service.MusicService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/play")
public class PlayServlet extends HttpServlet {
    private MusicService service = new MusicService();
    private CommentDao commentDao = new CommentDao();
    private MusicDao musicDao = new MusicDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if(idStr != null) {
            int currentMusicId = Integer.parseInt(idStr);

            // 1. 获取音乐详情 (并增加播放量)
            Music music = service.play(currentMusicId);

            HttpSession session = req.getSession();
            User user = (User) session.getAttribute("user");

            // ✨ 算法3：记录跳转序列 (A -> B)
            Integer prevMusicId = (Integer) session.getAttribute("lastPlayedMusicId");
            if (user != null && prevMusicId != null && prevMusicId != currentMusicId) {
                // 如果是从别的歌切过来的，记录 A->B
                musicDao.updateUserSequence(user.getId(), prevMusicId, currentMusicId);
            }
            // 更新 Session，现在的 current 变成未来的 prev
            session.setAttribute("lastPlayedMusicId", currentMusicId);

            // ✨ 获取用户评分状态 (✨✨✨ 修复图标假亮的关键逻辑 ✨✨✨)
            double myScore = 0.0;
            int isExplicit = 0; // 新增：是否显性操作过
            if (user != null) {
                myScore = musicDao.getMusicPreferenceValue(user.getId(), currentMusicId);
                // 获取真实的显性状态 (1=点过, 0=没点过)
                isExplicit = musicDao.getMusicExplicitStatus(user.getId(), currentMusicId);
            }
            req.setAttribute("myScore", myScore);
            req.setAttribute("isExplicit", isExplicit); // 必须传给前端！

            // ✨ 获取推荐列表 (使用播放页专用逻辑：序列优先)
            List<Music> recommendList;
            if (user != null) {
                recommendList = musicDao.getRecommendationForPlayer(user.getId(), currentMusicId);
            } else {
                recommendList = musicDao.getRecommendationForGuest(); // 游客走全站热度
            }

            // 截取前 10 首
            if (recommendList.size() > 10) {
                recommendList = recommendList.subList(0, 10);
            }
            req.setAttribute("recommendList", recommendList);

            // 3. 获取评论
            req.setAttribute("commentList", commentDao.getCommentsByMusicId(currentMusicId));

            req.setAttribute("m", music);
            req.getRequestDispatcher("/player.jsp").forward(req, resp);
        }
    }
}