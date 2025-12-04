package com.music.controller;

import com.google.gson.Gson;
import com.music.bean.Music;
import com.music.service.MusicService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboardData")
public class DashboardDataServlet extends HttpServlet {
    private MusicService service = new MusicService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置返回格式为 JSON
        resp.setContentType("application/json;charset=UTF-8");

        // 1. 获取前 10 名热歌
        List<Music> hotList = service.getHotList(); // 假设这个方法返回的是 Top 20，我们可以截取前10
        if(hotList.size() > 10) {
            hotList = hotList.subList(0, 10);
        }

        // 2. 准备两个数组：一个存歌名，一个存播放量 (ECharts 需要的数据格式)
        List<String> names = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (Music m : hotList) {
            names.add(m.getTitle());
            counts.add(m.getPlayCount());
        }

        // 3. 封装成 Map
        Map<String, Object> data = new HashMap<>();
        data.put("names", names);
        data.put("counts", counts);

        // 4. 转成 JSON 输出
        // 结果类似：{"names":["歌名1","歌名2"], "counts":[100, 88]}
        String json = new Gson().toJson(data);
        resp.getWriter().write(json);
    }
}