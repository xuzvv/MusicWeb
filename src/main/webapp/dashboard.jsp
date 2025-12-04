<%--
  Created by IntelliJ IDEA.
  User: w
  Date: 2025/12/5
  Time: 00:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>数据驾驶舱</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/echarts/5.4.3/echarts.min.js"></script>
    <style>
        body { background: #f0f2f5; font-family: "Microsoft YaHei", sans-serif; padding: 20px; }
        .chart-box {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .header { display: flex; justify-content: space-between; margin-bottom: 20px;}
        a { text-decoration: none; color: #007bff; }
    </style>
</head>
<body>
<div style="width: 1000px; margin: 0 auto;">
    <div class="header">
        <h2>📊 平台数据监控驾驶舱</h2>
        <div>
            <a href="admin">返回管理后台</a> | <a href="index">返回首页</a>
        </div>
    </div>

    <div class="chart-box">
        <h3 style="text-align:center;">🔥 全站热歌 TOP 10 (实时数据)</h3>
        <div id="main" style="width: 100%; height: 500px;"></div>
    </div>
</div>

<script>
    // 1. 初始化 ECharts 实例
    var myChart = echarts.init(document.getElementById('main'));

    // 2. 配置项 (初始为空，等数据来了再填)
    var option = {
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
            type: 'category',
            data: [], // 待填入歌名
            axisLabel: { interval: 0, rotate: 30 } // 歌名倾斜防止重叠
        },
        yAxis: { type: 'value', name: '播放次数' },
        series: [{
            data: [], // 待填入播放量
            type: 'bar', // 柱状图
            showBackground: true,
            backgroundStyle: { color: 'rgba(180, 180, 180, 0.2)' },
            itemStyle: {
                // 渐变色，看起来更高级
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#83bff6' },
                    { offset: 0.5, color: '#188df0' },
                    { offset: 1, color: '#188df0' }
                ])
            }
        }]
    };

    // 3. 使用 AJAX (fetch) 获取后端 Servlet 数据
    fetch('dashboardData')
        .then(response => response.json())
        .then(data => {
            // 拿到数据，更新图表
            myChart.setOption({
                xAxis: { data: data.names },
                series: [{ data: data.counts }]
            });
        })
        .catch(error => console.error('Error:', error));

    // 渲染图表
    myChart.setOption(option);
</script>
</body>
</html>