/*
 * Echo · 回声 V5.0 - 完整数据库脚本
 * 包含：基础业务表 + 三大推荐算法核心表
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表 (基础信息)
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
                          `id` int NOT NULL AUTO_INCREMENT,
                          `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'user',
                          `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'https://api.dicebear.com/7.x/identicon/svg?seed=Felix',
                          `bio` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '这个人很懒，什么都没写',
                          `social_link` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- 2. 音乐表 (整合算法 2 和 3 的全局分值)
-- ----------------------------
DROP TABLE IF EXISTS `music`;
CREATE TABLE `music`  (
                          `id` int NOT NULL AUTO_INCREMENT,
                          `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `artist` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `play_count` int NULL DEFAULT 0,
                          `upload_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                          `status` int NULL DEFAULT 0 COMMENT '0:待审核, 1:已通过',
                          `uploader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'System',
                          `duration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '03:45',
    -- 新增：算法核心字段
                          `duration_seconds` int DEFAULT 0 COMMENT '歌曲总时长(秒)',
                          `total_preference_sum` decimal(10, 2) DEFAULT 0.00 COMMENT '全局总喜爱度',
                          `selection_count` int DEFAULT 0 COMMENT '被作为下一首选取的总次数',
                          `recommendation_score` decimal(10, 2) DEFAULT 0.00 COMMENT '综合推荐度分值',
                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- 3. 个人偏好记录表 (算法 1 核心)
-- ----------------------------
DROP TABLE IF EXISTS `music_preference`;
CREATE TABLE `music_preference` (
                                    `user_id` int NOT NULL,
                                    `music_id` int NOT NULL,
                                    `total_duration` int NOT NULL COMMENT '当前计算时的总时长(秒)',
                                    `preference_value` decimal(3, 2) DEFAULT 0.00 COMMENT '喜爱度 [-1, 1]',
                                    `exit_time` int DEFAULT 0 COMMENT '离开页面时的播放时长(秒)',
                                    `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 共同主键：用户ID + 音乐ID + 时长
                                    PRIMARY KEY (`user_id`, `music_id`, `total_duration`),
                                    CONSTRAINT `fk_pref_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                    CONSTRAINT `fk_pref_music` FOREIGN KEY (`music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

-- ----------------------------
-- 4. 听歌习惯/点击行为表 (算法 3 核心)
-- ----------------------------
DROP TABLE IF EXISTS `music_sequence_habits`;
CREATE TABLE `music_sequence_habits` (
                                         `user_id` int NOT NULL,
                                         `current_music_id` int NOT NULL COMMENT '当前正在听的歌(ID-1)',
                                         `next_music_id` int NOT NULL COMMENT '点击切换到的下一首歌(ID-2)',
                                         `occurrence_count` int DEFAULT 1 COMMENT '该路径积累次数',
                                         `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 共同主键：用户+当前歌+下一首歌
                                         PRIMARY KEY (`user_id`, `current_music_id`, `next_music_id`),
                                         CONSTRAINT `fk_seq_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                         CONSTRAINT `fk_seq_curr` FOREIGN KEY (`current_music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE,
                                         CONSTRAINT `fk_seq_next` FOREIGN KEY (`next_music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

-- ----------------------------
-- 5. 其他业务表 (沿用初始脚本)
-- ----------------------------
DROP TABLE IF EXISTS `invite_codes`;
CREATE TABLE `invite_codes`  (
                                 `id` int NOT NULL AUTO_INCREMENT,
                                 `code` varchar(50) NOT NULL,
                                 `is_used` tinyint NULL DEFAULT 0,
                                 PRIMARY KEY (`id`), UNIQUE INDEX `code`(`code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
                             `id` int NOT NULL AUTO_INCREMENT,
                             `music_id` int NOT NULL,
                             `username` varchar(50) NOT NULL,
                             `content` text,
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`), FOREIGN KEY (`music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
                            `id` int NOT NULL AUTO_INCREMENT,
                            `music_id` int NOT NULL,
                            `content` varchar(255) NOT NULL,
                            `video_time` decimal(10, 2) NOT NULL,
                            `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`), FOREIGN KEY (`music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

DROP TABLE IF EXISTS `articles`;
CREATE TABLE `articles`  (
                             `id` int NOT NULL AUTO_INCREMENT,
                             `music_id` int NOT NULL,
                             `content` text,
                             `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`), UNIQUE INDEX `music_id`(`music_id`),
                             FOREIGN KEY (`music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
                             `id` int NOT NULL AUTO_INCREMENT,
                             `sender_id` int NOT NULL,
                             `receiver_id` int NOT NULL,
                             `content` text NOT NULL,
                             `send_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                             `is_read` tinyint NULL DEFAULT 0,
                             PRIMARY KEY (`id`),
                             FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                             FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;