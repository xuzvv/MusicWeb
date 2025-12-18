package com.music.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@WebServlet("/chat")
public class AIServlet extends HttpServlet {
    // ⚠️⚠️⚠️ 请去 deepseek 官网申请 API Key 填入这里
    private static final String API_KEY = "sk-fac72cd052b444c0ab471f8895013282";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // 1. 获取前端发来的问题
        String userQuestion = req.getParameter("question");
        if (userQuestion == null || userQuestion.trim().isEmpty()) return;

        // 2. 构建 DeepSeek 请求体 (JSON)
        // 提示词工程：设定人设为“资深音乐评论家”
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", "你是一个资深音乐评论家，名字叫'X² Voice AI'。请用简短、专业的语气回答用户关于音乐、歌手、乐理的问题。如果问题与音乐无关，请礼貌拒绝。");

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userQuestion);

        JsonArray messages = new JsonArray();
        messages.add(systemMsg);
        messages.add(userMsg);

        JsonObject payload = new JsonObject();
        payload.addProperty("model", "deepseek-chat");
        payload.add("messages", messages);
        payload.addProperty("stream", false); // 简单起见，不使用流式输出

        // 3. 发送请求给 DeepSeek
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(payload.toString(), MediaType.get("application/json")))
                .build();

        // 4. 处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // 解析 DeepSeek 返回的复杂 JSON，只提取 content
                String responseBody = response.body().string();
                JsonObject jsonResp = new Gson().fromJson(responseBody, JsonObject.class);
                String content = jsonResp.getAsJsonArray("choices").get(0).getAsJsonObject()
                        .getAsJsonObject("message").get("content").getAsString();

                // 返回给前端
                JsonObject result = new JsonObject();
                result.addProperty("answer", content);
                resp.getWriter().write(result.toString());
            } else {
                resp.getWriter().write("{\"answer\": \"AI 大脑过载中，请稍后再试...\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"answer\": \"连接 AI 服务器失败。\"}");
        }
    }
}