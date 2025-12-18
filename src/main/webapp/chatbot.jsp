<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/5
  Time: 13:38
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<div id="ai-bubble" onclick="toggleChat()" style="position: fixed; bottom: 30px; right: 30px; width: 60px; height: 60px; background: linear-gradient(135deg, #00c6ff, #0072ff); border-radius: 50%; box-shadow: 0 4px 15px rgba(0,114,255,0.4); cursor: pointer; z-index: 9999; display: flex; align-items: center; justify-content: center; transition: transform 0.3s;">
    <span style="font-size: 30px;">🤖</span>
</div>

<div id="ai-window" style="position: fixed; bottom: 100px; right: 30px; width: 350px; height: 500px; background: white; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.15); z-index: 9999; display: none; flex-direction: column; overflow: hidden;">
    <div style="background: #0072ff; color: white; padding: 15px; font-weight: bold; display: flex; justify-content: space-between;">
        <span>🎵 音乐百科助手</span>
        <span onclick="toggleChat()" style="cursor: pointer;">✖</span>
    </div>

    <div id="chat-messages" style="flex: 1; padding: 15px; overflow-y: auto; background: #f9f9f9; font-size: 14px;">
        <div style="margin-bottom: 10px;">
            <div style="background: #fff; border: 1px solid #ddd; padding: 8px 12px; border-radius: 8px; display: inline-block; max-width: 80%;">
                你好！我是 X² Voice AI 音乐助手，有什么可以帮你的吗？
            </div>
        </div>
    </div>

    <div style="padding: 10px; border-top: 1px solid #eee; display: flex;">
        <input type="text" id="chat-input" placeholder="问问关于音乐的事..." style="flex: 1; padding: 8px; border: 1px solid #ddd; border-radius: 4px; outline: none;" onkeypress="if(event.keyCode==13) sendMsg()">
        <button onclick="sendMsg()" style="margin-left: 10px; background: #0072ff; color: white; border: none; padding: 0 15px; border-radius: 4px; cursor: pointer;">发送</button>
    </div>
</div>

<script>
    function toggleChat() {
        var win = document.getElementById("ai-window");
        var bubble = document.getElementById("ai-bubble");
        if (win.style.display === "none") {
            win.style.display = "flex";
            bubble.style.transform = "scale(0)"; // 隐藏球
        } else {
            win.style.display = "none";
            bubble.style.transform = "scale(1)"; // 显示球
        }
    }

    function sendMsg() {
        var input = document.getElementById("chat-input");
        var msg = input.value.trim();
        if (!msg) return;

        // 1. 显示用户提问
        appendMsg(msg, "user");
        input.value = "";

        // 2. 显示“思考中”
        var loadingId = appendMsg("正在思考中...", "ai");

        // 3. 请求后端 Servlet
        fetch("chat", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: "question=" + encodeURIComponent(msg)
        })
            .then(res => res.json())
            .then(data => {
                // 4. 更新回复
                var loadingBubble = document.getElementById(loadingId);
                if (loadingBubble) {
                    loadingBubble.innerText = data.answer;
                }
            })
            .catch(err => {
                var loadingBubble = document.getElementById(loadingId);
                if (loadingBubble) {
                    loadingBubble.innerText = "网络出小差了，请重试。";
                }
            });
    }

    function appendMsg(text, role) {
        var box = document.getElementById("chat-messages");
        var div = document.createElement("div");

        // 🔥 修改点2：核心修复！在时间戳后增加随机数，防止 ID 冲突导致气泡被吞 🔥
        var id = "msg-" + new Date().getTime() + "-" + Math.floor(Math.random() * 10000);

        div.style.marginBottom = "10px";
        div.style.textAlign = role === "user" ? "right" : "left";

        var inner = document.createElement("div");
        inner.id = id;
        inner.style.display = "inline-block";
        inner.style.padding = "8px 12px";
        inner.style.borderRadius = "8px";
        inner.style.maxWidth = "80%";
        inner.style.textAlign = "left"; // 内容始终左对齐
        inner.innerText = text;

        if (role === "user") {
            inner.style.background = "#0072ff";
            inner.style.color = "white";
        } else {
            inner.style.background = "#fff";
            inner.style.border = "1px solid #ddd";
            inner.style.color = "#333";
        }

        div.appendChild(inner);
        box.appendChild(div);
        box.scrollTop = box.scrollHeight; // 滚动到底部
        return id;
    }
</script>