<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>推荐算法实验室 - AI vs Rule-Based</title>
    <link rel="stylesheet" href="https://cdn.staticfile.org/twitter-bootstrap/4.3.1/css/bootstrap.min.css">
    <style>
        body { background-color: #f8f9fa; font-family: "Microsoft YaHei", sans-serif; }
        .lab-container { max-width: 900px; margin: 50px auto; }
        .card { border: none; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border-radius: 12px; }

        .score-panel { text-align: center; padding: 30px; border-radius: 12px; color: white; transition: all 0.3s; }
        .bg-old { background: linear-gradient(135deg, #6c757d, #495057); }
        .bg-new { background: linear-gradient(135deg, #007bff, #0056b3); box-shadow: 0 5px 15px rgba(0,123,255,0.4); }

        .score-val { font-size: 3.5rem; font-weight: bold; margin: 10px 0; }
        .score-desc { font-size: 1.1rem; opacity: 0.9; }

        .control-panel { padding: 30px; background: white; margin-bottom: 30px; }
        .slider-label { font-weight: bold; display: flex; justify-content: space-between; }
        input[type=range] { width: 100%; height: 8px; border-radius: 5px; background: #d3d3d3; outline: none; -webkit-appearance: none; }
        input[type=range]::-webkit-slider-thumb { -webkit-appearance: none; width: 20px; height: 20px; border-radius: 50%; background: #007bff; cursor: pointer; transition: 0.2s; }
        input[type=range]::-webkit-slider-thumb:hover { transform: scale(1.2); }
    </style>
</head>
<body>

<div class="container lab-container">
    <h2 class="text-center mb-2">🧪 音乐推荐算法实验室</h2>
    <p class="text-center text-muted mb-5">Real-time Algorithm Comparison: Rule-Based vs AI Logistic Regression</p>

    <div class="card control-panel">
        <h5 class="mb-4">🎛️ 模拟听歌行为</h5>

        <div class="form-group mb-4">
            <div class="slider-label">
                <span>🎵 歌曲总时长 (Duration)</span>
                <span class="text-primary" id="durDisplay">240 秒 (4:00)</span>
            </div>
            <input type="range" id="totalTime" min="60" max="900" value="240" step="10" oninput="updateDemo()">
            <small class="text-muted">拖动滑块模拟不同长度的歌曲（如 60s 短视频 BGM 或 600s 史诗长歌）</small>
        </div>

        <div class="form-group">
            <div class="slider-label">
                <span>⏱️ 实际播放时长 (Play Time)</span>
                <span class="text-success" id="playDisplay">120 秒</span>
            </div>
            <input type="range" id="playTime" min="0" max="900" value="120" step="5" oninput="updateDemo()">
            <div class="progress mt-2" style="height: 20px;">
                <div id="ratioBar" class="progress-bar bg-success progress-bar-striped progress-bar-animated" role="progressbar" style="width: 50%">50%</div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6 mb-4">
            <div class="score-panel bg-old">
                <h4>📜 旧算法 (Baseline)</h4>
                <div class="score-desc">线性切分 (阈值 0.5)</div>
                <div id="oldScore" class="score-val">0.00</div>
                <div id="oldComment" class="badge badge-light p-2">等待计算...</div>
                <div class="mt-3 small text-light">只看比例，不看时长</div>
            </div>
        </div>

        <div class="col-md-6 mb-4">
            <div class="score-panel bg-new">
                <h4>🤖 AI 模型 (V3.1)</h4>
                <div class="score-desc">逻辑回归 (Fix for Raw Data)</div>
                <div id="aiScore" class="score-val">0.00</div>
                <div id="aiComment" class="badge badge-light p-2">等待计算...</div>
                <div class="mt-3 small text-light">动态权重 (8.0, 0.002, -4.0)</div>
            </div>
        </div>
    </div>

    <div class="alert alert-warning text-center">
        <strong>💡 演示要点：</strong> 尝试将总时长设为 <b>600秒(10分钟)</b>，播放进度设为 <b>48% (约290秒)</b>。
        <br>旧算法会机械判负(-0.48)，而 AI 会因为时长奖励给出正向反馈！
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>

<script>
    // 页面加载完成后立即计算一次
    $(document).ready(function() {
        console.log("页面加载完成，初始化演示...");
        updateDemo();
    });

    function updateDemo() {
        let total = parseInt($("#totalTime").val());
        let play = parseInt($("#playTime").val());

        // 联动限制：播放时长不能超过总时长
        if (play > total) {
            play = total;
            $("#playTime").val(total);
        }

        // 更新显示文字
        $("#durDisplay").text(total + " 秒 (" + Math.floor(total/60) + ":" + (total%60).toString().padStart(2,'0') + ")");
        $("#playDisplay").text(play + " 秒");

        // 更新进度条
        let ratio = (play / total * 100).toFixed(1);
        $("#ratioBar").css("width", ratio + "%").text("播放比例: " + ratio + "%");

        // AJAX 请求后端计算 (使用绝对路径避免 404)
        let url = "${pageContext.request.contextPath}/algo-demo";

        $.post(url, { playTime: play, totalTime: total }, function(res) {
            // 渲染旧算法结果
            $("#oldScore").text(res.oldScore.toFixed(3));
            updateComment("#oldComment", res.oldScore);

            // 渲染 AI 算法结果
            $("#aiScore").text(res.aiScore.toFixed(3));
            updateComment("#aiComment", res.aiScore);
        }).fail(function() {
            console.error("请求失败，请检查 Servlet 是否已部署");
        });
    }

    function updateComment(selector, score) {
        let text = "";
        if (score > 0.8) text = "😍 极度喜爱";
        else if (score > 0.4) text = "😊 比较喜欢";
        else if (score > 0) text = "🙂 轻微好感";
        else if (score > -0.4) text = "😐 轻微无感";
        else if (score > -0.8) text = "😒 不太喜欢";
        else text = "👋 极度讨厌 (秒切)";
        $(selector).text(text);
    }
</script>
</body>
</html>