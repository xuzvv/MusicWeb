package com.music.bean;

public class Music {
    private int id;
    private String title;
    private String artist;
    private String filePath;
    private int playCount;
    private int status;
    private String uploaderName;
    private String duration;
    private String uploadTime;

    // 数据库扩展字段
    private int durationSeconds;
    private double totalPreferenceSum;
    private int selectionCount;
    private double recommendationScore;

    // 辅助字段
    private String uploaderNickname;

    // ✨✨✨ 新增：推荐类型 (red, green, mixed, normal) ✨✨✨
    private String recommendType;
    // ✨✨✨ 新增：个人喜好度 (用于排序比较) ✨✨✨
    private double personalPreference;

    // Getters and Setters
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
    public String getUploaderNickname() { return uploaderNickname; }
    public void setUploaderNickname(String uploaderNickname) { this.uploaderNickname = uploaderNickname; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public double getTotalPreferenceSum() { return totalPreferenceSum; }
    public void setTotalPreferenceSum(double totalPreferenceSum) { this.totalPreferenceSum = totalPreferenceSum; }
    public int getSelectionCount() { return selectionCount; }
    public void setSelectionCount(int selectionCount) { this.selectionCount = selectionCount; }
    public double getRecommendationScore() { return recommendationScore; }
    public void setRecommendationScore(double recommendationScore) { this.recommendationScore = recommendationScore; }

    // 新增字段的 Getter/Setter
    public String getRecommendType() { return recommendType; }
    public void setRecommendType(String recommendType) { this.recommendType = recommendType; }
    public double getPersonalPreference() { return personalPreference; }
    public void setPersonalPreference(double personalPreference) { this.personalPreference = personalPreference; }
}