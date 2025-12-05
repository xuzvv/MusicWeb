# 🎵 Echo · 回声 - 独立音乐创作社区 (V5.0 终极版)

> **"念念不忘，必有回响。"**
>
> 一个基于 JavaWeb 标准技术栈 (MVC + 三层架构) 开发的独立音乐分享与社交平台。
>
> **V5.0 版本** 集成了 **DeepSeek AI**、**WebSocket 实时通讯**（弹幕+私信）、**RBAC 权限体系** 以及 **现代化 UI**，是一个功能完备的 Web 全栈项目。

---

## ✨ 核心功能亮点 (Features)

### 🤖 1. AI 与智能交互
- **DeepSeek 音乐助手**：全站集成 AI 机器人（悬浮球），调用 DeepSeek 大模型 API，可实时回答音乐知识、推荐歌曲。
- **智能反馈**：支持自然语言对话，打造沉浸式音乐伴侣体验。

### 💬 2. 深度社交体系
- **私信聊天 (DM)**：基于 **WebSocket** 实现的点对点即时通讯。支持实时收发、消息持久化存储、离线消息查看。
- **消息中心 (Inbox)**：聚合展示最近联系人列表，实时显示未读消息红点提醒。
- **个人主页 (Profile)**：独立的用户空间，展示头像、昵称、简介、社交链接及该用户发布的所有作品。
- **资料编辑**：用户可自定义修改个人资料（昵称、头像URL、Bio等）。

### 🎨 3. 极致视听体验
- **APlayer 高颜值播放器**：集成封面显示、歌词滚动、波形进度条，替代原生丑陋控件。
- **时间轴弹幕**：B站同款逻辑，支持弹幕回溯（空降）、实时广播，多用户同屏互动。
- **评论互动**：支持发表评论，点击头像可直达用户主页或发起私信。

### 🛠️ 4. 业务架构升级
- **多维度榜单**：首页重构为 **“热门榜单”**、**“最新发布”**、**“猜你喜欢”** 三大标签页。
- **数据分页**：后端实现 `LIMIT` 分页查询，前端配合分页条，支持海量数据展示。
- **推荐算法**：播放页侧边栏集成“猜你喜欢”模块，随机推荐相关音乐。

### 🛡️ 5. 权限与安全
- **邀请码注册**：注册需校验邀请码（如 `ECHO2025`），管理员后台可动态生成/删除邀请码。
- **内容审核**：用户上传音乐默认为“待审核”状态，需管理员批准后上线。
- **数据大屏**：后台集成 **ECharts**，管理员可查看全站热歌数据的动态柱状图。

---

## 🛠️ 技术栈 (Tech Stack)

| 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **后端核心** | Java (JDK 1.8) | Servlet 3.1, JSP, JDBC, MVC架构 |
| **实时通信** | **WebSocket** | 用于弹幕广播 (Broadcast) 和 私信单播 (Unicast) |
| **AI 接口** | **OkHttp 4.x** | 请求 DeepSeek API |
| **数据交互** | **AJAX / Gson** | 前后端异步数据传输 (JSON) |
| **前端视图** | HTML5, CSS3 | APlayer, ECharts, 响应式布局 |
| **数据库** | MySQL 5.7 / 8.0 | 多表关联 (User, Music, Message, Comment...) |
| **服务器** | Tomcat 9.0 | 支持 Servlet 3.1 和 WebSocket |

---

## 🚀 快速部署指南 (Deployment)

### 1. 数据库初始化
请务必在 MySQL 中执行项目根目录下的 **`musicdb.sql`** 脚本。
> ⚠️ **警告**：V5.0 数据库包含 `messages` (私信)、`invite_codes` (邀请码)、`articles` (手记) 等新表，请务必重建数据库以防报错。

### 2. 核心配置 (Config)

#### A. 数据库连接
打开 `src/main/java/com/music/util/DBUtil.java`：
```java
// 本地运行用 localhost，飞牛/Docker 部署请填局域网 IP
private static final String URL = "jdbc:mysql://localhost:3306/musicdb?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";
private static final String USER = "root";
private static final String PASS = "你的数据库密码"; // ⚠️ 必改！
```
#### B. AI 机器人配置
打开 `src/main/java/com/music/controller/AIServlet.java`：
```java
// ⚠️ 必须填写有效的 API Key，否则机器人无法回复
private static final String API_KEY = "sk-你的DeepSeek-API-Key"; 
```
### 3. 启动运行
1. 打开 IDEA，导入项目后，等待 Maven 自动下载并构建所需依赖。
2. 配置 Tomcat 9.0 服务器，部署 Artifact 为 `MusicWeb:war exploded`。
3. ⚠️ 关键配置：**Application Context（访问路径）必须设置为 `/MusicWeb`**，否则会导致接口调用、页面跳转异常。
4. 启动 Tomcat 服务器，在浏览器中输入地址：`http://localhost:8080/MusicWeb`，即可访问项目。

---

## 🧪 测试账号与资源
### 👤 默认账号
| 角色       | 账号    | 密码     | 核心权限                                                                 |
|------------|---------|----------|--------------------------------------------------------------------------|
| 超级管理员 | admin   | 123456   | 审核音乐内容、生成/删除邀请码、删除违规内容、查看全站数据大屏             |
| 普通用户   | -       | -        | 需使用邀请码注册，注册后可上传音乐、发送私信、发表评论/弹幕、编辑个人主页 |

### 🔑 注册邀请码
可用邀请码（直接复制使用）：
>ECHO2025
> MUSIC666
> VIP888


---

## 📂 核心目录结构
```plaintext
src/main/java/com/music/
├── bean/          # 实体类（封装数据模型：User、Music、Message、Danmaku 等）
├── dao/           # 数据访问层（数据库交互：UserDao、MessageDao、InviteDao 等）
├── service/       # 业务逻辑层（处理核心业务：用户认证、音乐审核、消息推送等）
├── controller/    # 控制器（接收请求、响应结果）
│   ├── ChatServer.java       # 私信功能 - WebSocket 单播实现
│   ├── DanmakuServer.java    # 弹幕功能 - WebSocket 广播实现
│   ├── AIServlet.java        # AI 助手 - DeepSeek API 代理接口
│   ├── HomeServlet.java      # 首页 - 榜单展示、分页查询逻辑
│   └── ...                   # 其他业务控制器（用户、音乐、评论等）
└── util/          # 工具类（通用工具：DBUtil 数据库连接、参数校验等）
```
## 📝 关于项目
本项目为 JavaWeb 课程设计高分作品，采用 **MVC 设计模式 + 三层架构** 构建，覆盖从基础业务实现到高级功能集成的全栈开发链路，兼具实用性与规范性，适合作为 JavaWeb 实战学习或课程设计参考案例。

### 核心价值
- 技术覆盖全面：串联 JavaWeb 核心技术栈，落地从基础 CRUD 到实时通讯、AI 集成的完整开发流程
- 工程化设计：遵循分层架构思想，配套权限管控、数据校验、配置化部署等企业级开发规范
- 功能贴近场景：聚焦独立音乐社区核心需求，平衡基础功能与进阶交互体验

### 功能模块分类
| 模块类型 | 具体内容 |
|----------|----------|
| 基础能力 | 用户注册/登录、音乐上传/播放、评论互动、个人主页管理 |
| 进阶功能 | WebSocket 实时通讯（私信单播 + 弹幕广播）、DeepSeek AI 智能助手（音乐知识问答 + 歌曲推荐） |
| 工程规范 | RBAC 权限管控（管理员/普通用户）、数据库分页查询、用户内容审核、配置化参数管理（数据库/API Key） |