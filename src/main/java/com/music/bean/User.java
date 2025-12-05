package com.music.bean;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;

    // V5.0 新增字段
    private String nickname;
    private String avatar;
    private String bio;
    private String socialLink;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSocialLink() { return socialLink; }
    public void setSocialLink(String socialLink) { this.socialLink = socialLink; }
}