package com.campus.service;

import com.campus.model.Resume;
import com.campus.repository.ResumeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    public Resume uploadResume(Long userId, MultipartFile file, String name) throws Exception {
        // 检查文件类型
        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("只支持PDF文件");
        }

        // 确保上传目录存在
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + ".pdf";
        Path filePath = Paths.get(uploadDir, fileName);
        
        // 保存文件到本地
        Files.copy(file.getInputStream(), filePath);

        // 解析PDF内容
        String content = extractPdfContent(file.getInputStream());

        // 生成访问URL
        String fileUrl = accessPath + "/" + fileName;

        // 删除旧的简历文件（如果存在）
        Resume oldResume = resumeRepository.findByStudentId(userId);
        if (oldResume != null) {
            String oldFileName = oldResume.getAttachmentUrl().substring(oldResume.getAttachmentUrl().lastIndexOf("/") + 1);
            Files.deleteIfExists(Paths.get(uploadDir, oldFileName));
            oldResume.setName(name);
            oldResume.setAttachmentUrl(fileUrl);
            oldResume.setContent(content);
            oldResume.setUpdateTime(LocalDateTime.now());
            return resumeRepository.save(oldResume);
        }

        // 保存新简历记录
        Resume resume = new Resume();
        resume.setStudentId(userId);
        resume.setName(name);
        resume.setAttachmentUrl(fileUrl);
        resume.setContent(content);
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());

        return resumeRepository.save(resume);
    }

    public String getResumeContent(Long userId) throws Exception {
        Resume resume = resumeRepository.findByStudentId(userId);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        return resume.getContent();
    }

    public byte[] getResumePdf(Long userId) throws Exception {
        Resume resume = resumeRepository.findByStudentId(userId);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        String fileName = resume.getAttachmentUrl().substring(resume.getAttachmentUrl().lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir, fileName);

        return Files.readAllBytes(filePath);
    }

    public void deleteResume(Long userId) throws Exception {
        Resume resume = resumeRepository.findByStudentId(userId);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        // 删除文件
        String fileName = resume.getAttachmentUrl().substring(resume.getAttachmentUrl().lastIndexOf("/") + 1);
        Files.deleteIfExists(Paths.get(uploadDir, fileName));

        // 删除数据库记录
        resumeRepository.delete(resume);
    }

    public Resume getResume(Long userId) {
        Resume resume = resumeRepository.findByStudentId(userId);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        return resume;
    }

    private String extractPdfContent(java.io.InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
} 