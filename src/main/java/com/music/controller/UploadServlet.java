package com.music.controller;

import com.music.bean.Music;
import com.music.bean.User;
import com.music.service.MusicService;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private MusicService service = new MusicService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        String title = req.getParameter("title");
        String artist = req.getParameter("artist");
        Part filePart = req.getPart("file");

        String fileName = UUID.randomUUID().toString() + ".mp3";
        String uploadPath = getServletContext().getRealPath("/uploads");
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String fullPath = uploadPath + File.separator + fileName;
        filePart.write(fullPath);

        // 核心修复：解析秒数
        String durationStr = "00:00";
        int durationSeconds = 0;
        try {
            File mp3File = new File(fullPath);
            AudioFile audioFile = AudioFileIO.read(mp3File);
            durationSeconds = audioFile.getAudioHeader().getTrackLength(); // 获取秒数
            durationStr = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("音频解析失败，使用默认时长");
        }

        Music m = new Music();
        m.setTitle(title);
        m.setArtist(artist);
        m.setFilePath("uploads/" + fileName);
        m.setUploaderName(user.getUsername());
        m.setDuration(durationStr);
        m.setDurationSeconds(durationSeconds); // 存入秒数

        service.upload(m);

        resp.sendRedirect("index");
    }
}