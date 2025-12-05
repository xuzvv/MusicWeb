/*
 * MusicWeb 数据库完整脚本 (V3.0)
 * 包含：用户、音乐、评论、弹幕
 */

CREATE DATABASE IF NOT EXISTS musicdb DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE musicdb;

-- 清理旧表 (注意删除顺序)
DROP TABLE IF EXISTS danmaku;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS music;
DROP TABLE IF EXISTS users;

-- 1. 用户表
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(50) NOT NULL,
                       role VARCHAR(20) DEFAULT 'user'
);
INSERT INTO users (username, password, role) VALUES ('admin', '123456', 'admin');

-- 2. 音乐表
CREATE TABLE music (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       title VARCHAR(100) NOT NULL,
                       artist VARCHAR(50) NOT NULL,
                       file_path VARCHAR(255) NOT NULL,
                       play_count INT DEFAULT 0,
                       upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       status INT DEFAULT 0 COMMENT '0:待审核, 1:已通过',
                       uploader_name VARCHAR(50) DEFAULT 'System'
);
INSERT INTO music (title, artist, file_path, play_count, status, uploader_name)
VALUES ('测试歌曲', 'System', 'test.mp3', 100, 1, 'admin');

-- 3. 评论表
CREATE TABLE comments (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          music_id INT NOT NULL,
                          username VARCHAR(50),
                          content TEXT,
                          create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
);

-- 4. 弹幕表 (新增)
CREATE TABLE danmaku (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         music_id INT NOT NULL,
                         content VARCHAR(255) NOT NULL,
                         video_time DECIMAL(10, 2) NOT NULL COMMENT '弹幕时间点(秒)',
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
);