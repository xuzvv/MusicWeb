package com.music.bean;

public class Music {
    private int id;
    private String title;
    private String artist;
    private String filePath;
    private int playCount;
    private int status;
    private String uploaderName; // 存的是 username (唯一账号)
    private String duration;
    private String uploadTime;

    // ✨✨✨ 新增：作者昵称 (用于展示) ✨✨✨
    private String uploaderNickname;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }

    // ✨ Getter/Setter ✨
    public String getUploaderNickname() { return uploaderNickname; }
    public void setUploaderNickname(String uploaderNickname) { this.uploaderNickname = uploaderNickname; }
}