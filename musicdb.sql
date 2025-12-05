/*
 * Echo · 回声 V5.0 - 完整数据库脚本
 * 包含：用户资料扩展、邀请码、音乐时长、多榜单支持、私信系统
 */

-- 1. 重建数据库
DROP DATABASE IF EXISTS musicdb;
CREATE DATABASE musicdb DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE musicdb;

-- 2. 用户表 (新增昵称、简介、头像、社交链接)
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(50) NOT NULL,
                       role VARCHAR(20) DEFAULT 'user',
                       nickname VARCHAR(50),
                       avatar VARCHAR(255) DEFAULT 'https://api.dicebear.com/7.x/identicon/svg?seed=Felix', -- 默认随机头像API
                       bio VARCHAR(200) DEFAULT '这个人很懒，什么都没写',
                       social_link VARCHAR(200)
);
-- 插入管理员 (无需邀请码)
INSERT INTO users (username, password, role, nickname, bio) VALUES ('admin', '123456', 'admin', '站长', '系统维护者');

-- 3. 邀请码表 (新增)
CREATE TABLE invite_codes (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              code VARCHAR(50) UNIQUE NOT NULL,
                              is_used TINYINT DEFAULT 0 COMMENT '0:未使用, 1:已使用'
);
-- 初始化测试邀请码
INSERT INTO invite_codes (code) VALUES ('ECHO2025'), ('MUSIC666'), ('VIP888');

-- 4. 音乐表 (新增 duration 时长字段)
CREATE TABLE music (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       title VARCHAR(100) NOT NULL,
                       artist VARCHAR(50) NOT NULL,
                       file_path VARCHAR(255) NOT NULL,
                       play_count INT DEFAULT 0,
                       upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       status INT DEFAULT 0 COMMENT '0:待审核, 1:已通过',
                       uploader_name VARCHAR(50) DEFAULT 'System',
                       duration VARCHAR(20) DEFAULT '03:45'
);
-- 插入测试数据
INSERT INTO music (title, artist, file_path, play_count, status, uploader_name, duration)
VALUES ('测试歌曲1', 'System', 'test.mp3', 100, 1, 'admin', '04:20');
INSERT INTO music (title, artist, file_path, play_count, status, uploader_name, duration)
VALUES ('测试歌曲2', 'System', 'test.mp3', 50, 1, 'admin', '03:10');
INSERT INTO music (title, artist, file_path, play_count, status, uploader_name, duration)
VALUES ('待审核歌曲', 'User', 'test.mp3', 0, 0, 'admin', '02:50');

-- 5. 评论表
CREATE TABLE comments (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          music_id INT NOT NULL,
                          username VARCHAR(50) NOT NULL,
                          content TEXT,
                          create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
);

-- 6. 弹幕表
CREATE TABLE danmaku (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         music_id INT NOT NULL,
                         content VARCHAR(255) NOT NULL,
                         video_time DECIMAL(10, 2) NOT NULL,
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
);

-- 7. 文章表 (创作手记)
CREATE TABLE articles (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          music_id INT NOT NULL UNIQUE,
                          content TEXT,
                          update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
);

-- 8. 私信消息表 (新增)
CREATE TABLE messages (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          sender_id INT NOT NULL COMMENT '发送者ID',
                          receiver_id INT NOT NULL COMMENT '接收者ID',
                          content TEXT NOT NULL COMMENT '聊天内容',
                          send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          is_read TINYINT DEFAULT 0 COMMENT '0:未读, 1:已读',
                          FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
                          FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 插入一条测试私信 (admin 发给 admin 自己)
INSERT INTO messages (sender_id, receiver_id, content) VALUES (1, 1, '欢迎来到 Echo 音乐社区！');