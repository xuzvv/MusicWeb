package com.music.service;

import com.music.bean.Music;
import com.music.dao.MusicDao;
import java.util.List;

public class MusicService {
    private MusicDao dao = new MusicDao();

    // 获取全站热榜 (用于首页侧边栏等)
    public List<Music> getHotList() {
        return dao.getHotMusic();
    }

    // 上传音乐
    public void upload(Music music) {
        dao.saveMusic(music);
    }

    // 播放逻辑：既要加播放量，又要查详情
    public Music play(int id) {
        // 1. 播放量 +1
        dao.addPlayCount(id);
        // 2. 返回歌曲详情
        return dao.getMusicById(id);
    }

    // 获取待审核列表
    public List<Music> getPendingList() {
        return dao.getPendingMusic();
    }

    // 获取所有音乐列表 (后台管理用)
    public List<Music> getAllList() {
        return dao.getAllMusic();
    }

    // 审核通过
    public void approve(int id) {
        dao.approveMusic(id);
    }

    // 删除音乐
    public void delete(int id) {
        dao.deleteMusic(id);
    }
}