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
import java.util.ArrayList;

@WebServlet("/play")
public class PlayServlet extends HttpServlet {
    private MusicService service = new MusicService();
    private CommentDao commentDao = new CommentDao();
    private MusicDao musicDao = new MusicDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if(idStr != null) {
            try {
                int currentMusicId = Integer.parseInt(idStr);

                // 1. 获取音乐详情 (Service层处理：增加播放量 + 查详情)
                Music music = service.play(currentMusicId);

                // 如果ID不存在或歌曲已下架，防止空指针
                if (music == null) {
                    resp.sendRedirect("index.jsp"); // 或者报错页面
                    return;
                }

                HttpSession session = req.getSession();
                User user = (User) session.getAttribute("user");

                // ================== ✨ 算法3：记录跳转序列 (A -> B) ==================
                // 逻辑：只有登录用户才记录由于“行为”产生的序列
                Integer prevMusicId = (Integer) session.getAttribute("lastPlayedMusicId");

                if (user != null && prevMusicId != null && prevMusicId != currentMusicId) {
                    // 如果是从别的歌切过来的，且不是刷新当前页，记录 A->B
                    musicDao.updateUserSequence(user.getId(), prevMusicId, currentMusicId);
                }

                // 更新 Session，现在的 current 变成未来的 prev，供下一次跳转使用
                session.setAttribute("lastPlayedMusicId", currentMusicId);

                // ================== ✨ 获取用户评分状态 (修复图标假亮) ==================
                double myScore = 0.0;
                int isExplicit = 0; // 0=没点过, 1=显性点过(红心)
                if (user != null) {
                    // 获取该用户对这首歌的喜爱值
                    myScore = musicDao.getMusicPreferenceValue(user.getId(), currentMusicId);
                    // 获取是否显性操作过（用于前端亮红心）
                    isExplicit = musicDao.getMusicExplicitStatus(user.getId(), currentMusicId);
                }
                req.setAttribute("myScore", myScore);
                req.setAttribute("isExplicit", isExplicit);

                // ================== ✨ 获取推荐列表 (差异化推荐核心) ==================
                List<Music> recommendList;
                if (user != null) {
                    // 🟢 情况A：登录用户 -> 查 Dao 的 getRecommendationForPlayer
                    // 这个方法会根据 userId 去查 music_sequence_habits，新号查不到就是空的，符合你的要求
                    recommendList = musicDao.getRecommendationForPlayer(user.getId(), currentMusicId);
                } else {
                    // ⚪ 情况B：游客 -> 查 Dao 的 getRecommendationForGuest
                    // 游客没有历史，只能看全站热度榜
                    recommendList = musicDao.getRecommendationForGuest();
                }

                // 列表截断：只展示前 10 首，保持页面整洁
                if (recommendList.size() > 10) {
                    recommendList = recommendList.subList(0, 10);
                }
                req.setAttribute("recommendList", recommendList);

                // 3. 获取评论列表
                req.setAttribute("commentList", commentDao.getCommentsByMusicId(currentMusicId));

                // 4. 转发到播放页
                req.setAttribute("m", music);
                req.getRequestDispatcher("/player.jsp").forward(req, resp);

            } catch (NumberFormatException e) {
                // 防止 id 乱填报错
                resp.sendRedirect("index.jsp");
            }
        } else {
            resp.sendRedirect("index.jsp");
        }
    }
}