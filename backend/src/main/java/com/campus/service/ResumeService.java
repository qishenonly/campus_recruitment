package com.campus.service;

import com.campus.model.Resume;
import com.campus.repository.ResumeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import com.campus.util.ResumeParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.io.IOException;
import java.util.NoSuchElementException;

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

        // 查找学生最新的简历
        List<Resume> oldResumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        Resume oldResume = oldResumes.isEmpty() ? null : oldResumes.get(0);
        
        if (oldResume != null) {
            // 如果存在旧简历，删除旧文件
            String oldFileName = oldResume.getAttachmentUrl().substring(oldResume.getAttachmentUrl().lastIndexOf("/") + 1);
            Files.deleteIfExists(Paths.get(uploadDir, oldFileName));
            oldResume.setName(name);
            oldResume.setAttachmentUrl(fileUrl);
            oldResume.setContent(content);
            oldResume.setUpdateTime(LocalDateTime.now());
            return resumeRepository.save(oldResume);
        }

        // 解析简历内容
        Map<String, Object> parsedContent = ResumeParser.extractDetails(file.getInputStream());
        Map<String, String> structuredInfo = (Map<String, String>) parsedContent.get("structuredInfo");
        

        // 保存新简历记录
        Resume resume = new Resume();
        resume.setStudentId(userId);
        resume.setName(name);
        resume.setAttachmentUrl(fileUrl);
        resume.setContent(content);
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());

        // 如果提供了名称，使用提供的名称，否则尝试从解析结果中获取
        if (name != null && !name.isEmpty()) {
            resume.setName(name);
        } else if (structuredInfo.containsKey("name")) {
            resume.setName(structuredInfo.get("name") + "的简历");
        } else {
            resume.setName("我的简历");
        }
        
        // 填充简历内容
        if (structuredInfo.containsKey("education")) {
            resume.setEducation(structuredInfo.get("education"));
        }
        
        if (structuredInfo.containsKey("workExperience")) {
            resume.setExperience(structuredInfo.get("workExperience"));
        }
        
        if (structuredInfo.containsKey("skills")) {
            resume.setSkills(structuredInfo.get("skills"));
        }
        
        if (structuredInfo.containsKey("projectExperience")) {
            resume.setProjects(structuredInfo.get("projectExperience"));
        }
        
        if (structuredInfo.containsKey("selfEvaluation")) {
            resume.setSelfEvaluation(structuredInfo.get("selfEvaluation"));
        }

        return resumeRepository.save(resume);
    }

    public String getResumeContent(Long userId) throws Exception {
        // 获取学生的最新简历
        List<Resume> resumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        if (resumes.isEmpty()) {
            throw new RuntimeException("简历不存在");
        }
        
        Resume resume = resumes.get(0);
        return resume.getContent();
    }

    public byte[] getResumePdf(Long userId) throws IOException {
        // 获取学生的最新简历
        List<Resume> resumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        if (resumes.isEmpty()) {
            throw new NoSuchElementException("简历不存在");
        }
        
        Resume resume = resumes.get(0);
        if (resume.getAttachmentUrl() == null || resume.getAttachmentUrl().isEmpty()) {
            throw new NoSuchElementException("简历附件URL不存在");
        }
        
        try {
            String fileName = resume.getAttachmentUrl().substring(resume.getAttachmentUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, fileName);
            
            if (!Files.exists(filePath)) {
                throw new NoSuchElementException("简历文件不存在，文件路径: " + filePath.toString());
            }
            
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            // 记录更详细的错误信息
            throw new NoSuchElementException("获取简历文件失败: " + e.getMessage() + ", 用户ID: " + userId);
        }
    }

    public void deleteResume(Long userId) throws Exception {
        // 获取学生的最新简历
        List<Resume> resumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        if (resumes.isEmpty()) {
            throw new RuntimeException("简历不存在");
        }
        
        Resume resume = resumes.get(0);

        // 删除文件
        String fileName = resume.getAttachmentUrl().substring(resume.getAttachmentUrl().lastIndexOf("/") + 1);
        Files.deleteIfExists(Paths.get(uploadDir, fileName));

        // 删除数据库记录
        resumeRepository.delete(resume);
    }

    public Resume getResume(Long userId) {
        // 获取学生的简历，如果有多份取最新的一份
        List<Resume> resumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        if (resumes.isEmpty()) {
            return null;
        }
        return resumes.get(0); // 返回最新的简历
    }

    private String extractPdfContent(java.io.InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private Resume getResumeByUserId(Long userId) {
        List<Resume> resumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        if (resumes.isEmpty()) {
            return null;
        }
        return resumes.get(0);
    }

    public Resume saveResume(Long userId, Resume resume) {
        // 获取学生以前的简历
        List<Resume> oldResumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        Resume oldResume = oldResumes.isEmpty() ? null : oldResumes.get(0);
        
        // 更新或创建简历
        if (oldResume != null) {
            resume.setId(oldResume.getId());
            resume.setCreateTime(oldResume.getCreateTime());
        } else {
            resume.setCreateTime(LocalDateTime.now());
        }
        
        // 设置其他字段...
        resume.setStudentId(userId);
        resume.setUpdateTime(LocalDateTime.now());
        
        return resumeRepository.save(resume);
    }

    public Resume updateResume(Long userId, String education, String skills, String experience, String projects, String awards, String selfEvaluation) {
        // 获取学生的简历
        List<Resume> resumes = resumeRepository.findByStudentIdOrderBySubmitTimeDesc(userId);
        if (resumes.isEmpty()) {
            throw new RuntimeException("简历不存在");
        }
        
        Resume resume = resumes.get(0);
        resume.setEducation(education);
        resume.setSkills(skills);
        resume.setExperience(experience);
        resume.setProjects(projects);
        resume.setAwards(awards);
        resume.setSelfEvaluation(selfEvaluation);
        resume.setUpdateTime(LocalDateTime.now());
        
        return resumeRepository.save(resume);
    }
} 