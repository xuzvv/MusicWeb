package com.music.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {
    // ✨✨✨ 关键修改：serverTimezone=Asia/Shanghai ✨✨✨
    // useSSL=false: 关闭SSL警告
    // allowPublicKeyRetrieval=true: 允许公钥检索(解决部分连接报错)
    // characterEncoding=utf-8: 防止乱码
    private static final String URL = "jdbc:mysql://localhost:3306/musicdb?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "password"; // 记得确认你的密码

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConn() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}