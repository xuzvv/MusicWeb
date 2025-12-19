这是一个为你量身定制的 **X² Voice (V5.0)** 项目 `README.md` 文档。

这份文档不仅包含了你现有的 JavaWeb 功能，还重点包装了我们这两天构建的 **“混合推荐算法”** 和 **“显隐性反馈机制”**。针对你提到的“防止网页切断”问题，我优化了排版，避免了过长的代码行和复杂的宽表格，确保在 GitHub 或任何 Markdown 阅读器中都能完美展示。

---

### 📄 README.md (建议文件名)

```markdown
# 🎵 X² Voice (V5.0) - 基于多源行为特征融合的智能音乐推荐平台

> **听见未知的频率。**
> An Intelligent Music Streaming Platform based on Hybrid Recommendation Algorithms.

![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Java](https://img.shields.io/badge/Java-8%2B-orange)
![Tech](https://img.shields.io/badge/Tech-Servlet%20%7C%20JSP%20%7C%20MySQL-lightgrey)
![Status](https://img.shields.io/badge/Status-Production-success)

## 📖 项目简介

**X² Voice** 是一个集音乐播放、社交互动与智能分发于一体的现代化流媒体平台。

不同于传统的 CRUD 音乐网站，本项目核心集成了一套 **基于显隐性反馈与马尔可夫链的混合推荐引擎**。系统能够在 **无人工标注（No-Tagging）** 的情况下，仅通过挖掘用户的播放时长、切歌行为和点赞交互，自动构建精准的个性化推荐列表。

项目已实现从 **前端埋点采集** 到 **后端算法计算** 再到 **数据库持久化** 的完整数据闭环，并支持云端（阿里云）与边缘端（NAS）的分布式部署。

---

## 🌟 核心亮点 (Core Features)

### 🧠 智能推荐引擎 (The Brain)
本系统摒弃了传统的随机推荐，采用了工业级的多层混合推荐策略：

#### 1. 算法 I：显隐性反馈融合模型
* **显性反馈 (Explicit)**：用户点击“👍喜欢”或“👎不感冒”拥有最高优先级（Priority Lock），直接决定评分正负。
* **隐性反馈 (Implicit)**：基于 **完播率（Completion Ratio）** 的动态评分。采用 **最大绝对值原则**，在多次播放中自动采信用户意图最强烈的行为（如秒切的厌恶或单曲循环的喜爱）。
* **防作弊机制**：引入 `is_explicit` 字段防止算法覆盖用户的主动评价。

#### 2. 算法 II：基于群体智慧的全局热度
* 聚合全站用户的喜爱度评分，而非单纯的播放量。识别出“叫好又叫座”的优质内容。
* **UI 表现**：🔥 **红色标签 (必听)**。

#### 3. 算法 III：基于一阶马尔可夫链的序列预测
* 捕捉用户 `Song A -> Song B` 的跳转行为，构建状态转移矩阵。
* 当用户播放 A 时，系统计算后续概率最高的 B 进行推荐。
* **UI 表现**：🚀 **绿色标签 (热门)**。

#### 4. 协同融合策略 (Hybrid Fusion)
* 当算法 II 与算法 III 命中同一首歌曲时，系统判定为 **🌟 超级推荐 (Mixed)**，给予最高展示权重。
* **冷启动方案**：新用户使用全局热度兜底，老用户使用个性化序列增强。

### 💻 完整功能模块
* **沉浸式播放器**：集成 APlayer，支持歌词同步、封面显示。
* **实时互动**：WebSocket 毫秒级弹幕、富文本评论社区。
* **用户中心**：个人主页、头像上传、资料编辑、私信系统。
* **创作者服务**：音乐上传（自动提取时长）、元数据管理。
* **后台管理**：用户管理、内容审核、数据看板。

---

## 🛠 技术栈 (Tech Stack)

* **后端**：Java (JDK 1.8+), Servlet 3.1, JSP, WebSocket
* **数据库**：MySQL 5.7 / 8.0 (使用了视图 View 与 触发器)
* **前端**：HTML5, CSS3 (Flex/Grid), JavaScript (ES6+), APlayer.js
* **构建工具**：Maven
* **服务器**：Tomcat 9.0
* **运维部署**：Aliyun ECS + 飞牛 NAS (内网穿透) + Nginx

---

## 💾 数据库设计 (Database Schema)

为了支撑推荐算法，系统在基础业务表之上构建了专用的数仓结构：

**1. `music_preference` (用户偏好表)**
存储用户对歌曲的评分 ($P \in [-1, 1]$)。
* `preference_value`: 核心评分
* `exit_time`: 离开时间
* `is_explicit`: 显性锁 (1=锁死, 0=算法更新)

**2. `music_sequence_habits` (序列习惯表)**
存储用户行为序列图。
* `current_music_id` -> `next_music_id`
* `occurrence_count`: 转移发生次数

**3. `music` (扩展表)**
集成 `total_preference_sum` (全站分) 与 `selection_count` (被选次数)，通过 SQL 聚合实时更新。

---

## 🚀 快速开始 (Quick Start)

### 1. 环境准备
* JDK 1.8+
* MySQL 5.7+
* Tomcat 9.0+
* IntelliJ IDEA (推荐)

### 2. 数据库初始化
1.  创建数据库 `musicdb`。
2.  导入项目根目录下的 **`musicdb.sql`** (或 `total.sql`)。
    * *注意：该脚本包含最新的算法表结构与触发器逻辑。*

### 3. 配置连接
修改 `src/main/java/com/music/util/DBUtil.java`:
```java
private static final String URL = 
    "jdbc:mysql://localhost:3306/musicdb?useUnicode=true&characterEncoding=utf-8";
private static final String USER = "root";
private static final String PASSWORD = "your_password";

```

### 4. 运行项目

1. 使用 Maven 导入依赖：`mvn clean install`。
2. 配置 Tomcat 并部署 `war` 包。
3. 访问：`http://localhost:8080/MusicWeb`。

---

## 📸 系统截图

| 首页推荐 (红绿标) | 沉浸式播放页 |
| --- | --- |
| *(在此处放置首页截图)* | *(在此处放置播放页截图)* |

| 数据可视化后台 | 实时弹幕互动 |
| --- | --- |
| *(在此处放置后台截图)* | *(在此处放置弹幕截图)* |

---

## ⚠️ 答辩与演示注意 (For Defense)

* **推荐演示**：
1. 登录账户，完整播放一首歌曲（或点击“👍喜欢”）。
2. 切歌或刷新首页，观察 **“🔥 必听”** 列表的实时变化。
3. 连续点击特定歌曲序列（如 A -> B），观察 **“🚀 热门”** 权重的提升。


* **冷启动**：若数据库为空，系统将自动回退至“默认ID排序”模式，保证页面不留白。随着用户使用数据积累，推荐将逐渐精准。

---

## 🤝 贡献与作者

* **Developer**: [你的名字]
* **Institution**: [你的学校]
* **Course**: JavaWeb 应用开发 / 人工智能基础

---

Copyright © 2025 X² Voice Studio. All Rights Reserved.

```

### 💡 使用建议：
1.  **图片替换**：把 `(在此处放置...截图)` 替换为你之前发给我的截图链接，或者直接在 GitHub 上上传图片后填入 URL。
2.  **提交**：将此内容保存为 `README.md`，覆盖你仓库根目录下的旧文件。

这份文档现在非常适合 GitHub 展示，代码块简洁，表格清晰，不会出现横向滚动条切断内容的情况。

```