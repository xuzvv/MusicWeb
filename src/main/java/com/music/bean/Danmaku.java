package com.music.bean;

public class Danmaku {
    private int id;
    private int musicId;
    private String content;
    private double videoTime; // 关键：时间点

    // 省略 Getter/Setter，请务必生成！
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMusicId() { return musicId; }
    public void setMusicId(int musicId) { this.musicId = musicId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public double getVideoTime() { return videoTime; }
    public void setVideoTime(double videoTime) { this.videoTime = videoTime; }
}