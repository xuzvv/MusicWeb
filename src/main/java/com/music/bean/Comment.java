package com.music.bean;

public class Comment {
    private int id;
    private int musicId;
    private String username;
    private String content;
    private String createTime;

    // ✨✨✨ 新增：用于展示的昵称字段 (数据库表里没有，是查询时联表查出来的) ✨✨✨
    private String nickname;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMusicId() { return musicId; }
    public void setMusicId(int musicId) { this.musicId = musicId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    // ✨✨✨ 新增 Getter/Setter ✨✨✨
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}