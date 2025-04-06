package com.campus.service.impl;

import com.campus.model.Resume;
import com.campus.repository.ResumeRepository;
import com.campus.service.ResumeParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParseServiceImpl implements ResumeParseService {

    private final ResumeRepository resumeRepository;
    
    @Value("${file.upload-dir}")
    private String uploadPath;
    
    @Override
    @Transactional
    public Resume uploadAndParseResume(MultipartFile file, Long studentId, String customResumeName) throws IOException {
        log.info("开始上传并解析简历(自定义名称): 学生ID={}, 文件名={}, 自定义名称={}, 文件大小={}bytes", 
                studentId, file.getOriginalFilename(), customResumeName, file.getSize());
        
        // 获取当前工作目录
        String currentDir = System.getProperty("user.dir");
        File workDir = new File(currentDir);
        log.info("当前工作目录: {}", workDir.getAbsolutePath());
        
        // 处理相对路径
        String finalUploadPath;
        File uploadDir;
        
        if (uploadPath.startsWith("./")) {
            // 相对于当前工作目录
            finalUploadPath = currentDir + uploadPath.substring(1);
            uploadDir = new File(finalUploadPath);
        } else if (uploadPath.startsWith("/")) {
            // 绝对路径
            finalUploadPath = uploadPath;
            uploadDir = new File(finalUploadPath);
        } else {
            // 默认相对于当前工作目录
            finalUploadPath = currentDir + File.separator + uploadPath;
            uploadDir = new File(finalUploadPath);
        }
        
        log.info("最终上传目录: {}", uploadDir.getAbsolutePath());
        
        // 1. 保存文件
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            log.error("文件名为空");
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 确定简历名称
        String resumeName;
        if (customResumeName != null && !customResumeName.trim().isEmpty()) {
            // 使用客户端提供的名称
            resumeName = customResumeName.trim();
        } else {
            // 从原始文件名获取简历名称（去掉扩展名）
            resumeName = originalFilename;
            if (resumeName.contains(".")) {
                resumeName = resumeName.substring(0, resumeName.lastIndexOf("."));
            }
        }
        log.info("最终简历名称: {}", resumeName);
        
        String fileExtension = getFileExtension(originalFilename);
        
        if (!fileExtension.equalsIgnoreCase("pdf")) {
            log.error("不支持的文件格式: {}", fileExtension);
            throw new IllegalArgumentException("仅支持PDF格式的简历文件");
        }
        
        // 创建上传目录
        if (!uploadDir.exists()) {
            log.info("上传目录不存在，尝试创建: {}", uploadDir.getAbsolutePath());
            boolean created = uploadDir.mkdirs();
            if (!created) {
                log.error("创建上传目录失败: {}", uploadDir.getAbsolutePath());
                throw new IOException("无法创建上传目录，请检查权限和路径: " + uploadDir.getAbsolutePath());
            }
            log.info("成功创建上传目录: {}", uploadDir.getAbsolutePath());
        } else {
            log.info("上传目录已存在: {}", uploadDir.getAbsolutePath());
            if (!uploadDir.canWrite()) {
                log.error("上传目录没有写入权限: {}", uploadDir.getAbsolutePath());
                throw new IOException("上传目录没有写入权限: " + uploadDir.getAbsolutePath());
            }
        }
        
        // 生成唯一文件名
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
        File destFile = new File(uploadDir, uniqueFilename);
        log.info("文件将保存为: {}", destFile.getAbsolutePath());
        
        // 保存文件
        try {
            file.transferTo(destFile);
            if (!destFile.exists()) {
                log.error("文件保存后不存在: {}", destFile.getAbsolutePath());
                throw new IOException("文件保存失败，无法验证文件存在性");
            }
            log.info("文件保存成功: {}, 大小: {}bytes", destFile.getAbsolutePath(), destFile.length());
        } catch (Exception e) {
            log.error("文件保存失败: {}", destFile.getAbsolutePath(), e);
            throw new IOException("保存文件失败: " + e.getMessage());
        }
        
        // 2. 解析PDF文件
        Map<String, String> extractedData;
        try {
            extractedData = extractDataFromPDF(destFile);
            log.info("PDF解析完成，提取到{}个字段", extractedData.size());
        } catch (IOException e) {
            log.error("PDF解析失败: {}", destFile.getAbsolutePath(), e);
            throw new IOException("无法解析PDF文件: " + e.getMessage());
        }
        
        // 3. 保存解析结果到简历表
        Resume resume = new Resume();
        resume.setStudentId(studentId);
        // 使用确定的简历名称
        resume.setName(resumeName);
        resume.setAttachmentUrl(destFile.getAbsolutePath());
        resume.setSubmitTime(LocalDateTime.now());
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());
        
        // 设置解析内容
        resume.setContent(extractedData.getOrDefault("fullContent", ""));
        
        // 设置解析出的具体字段
        resume.setPositionApplied(extractedData.getOrDefault("positionApplied", ""));
        resume.setEducation(extractedData.getOrDefault("education", ""));
        resume.setMajor(extractedData.getOrDefault("major", ""));
        resume.setPhone(extractedData.getOrDefault("phone", ""));
        resume.setEmail(extractedData.getOrDefault("email", ""));
        resume.setSchool(extractedData.getOrDefault("school", ""));
        resume.setGraduateYear(extractedData.getOrDefault("graduateYear", ""));
        resume.setExperience(extractedData.getOrDefault("workExperience", ""));
        resume.setSkills(extractedData.getOrDefault("skills", ""));
        resume.setProjects(extractedData.getOrDefault("projects", ""));
        resume.setAwards(extractedData.getOrDefault("awards", ""));
        resume.setSelfEvaluation(extractedData.getOrDefault("selfEvaluation", ""));
        
        // 保存解析数据
        try {
            Resume savedResume = resumeRepository.save(resume);
            log.info("简历保存成功: ID={}, 名称={}", savedResume.getId(), savedResume.getName());
            return savedResume;
        } catch (Exception e) {
            log.error("保存简历数据失败", e);
            throw new RuntimeException("保存简历数据失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Resume uploadAndParseResume(MultipartFile file, Long studentId) throws IOException {
        // 调用带名称参数的实现，但传入null作为名称，表示使用默认处理
        return uploadAndParseResume(file, studentId, null);
    }
    
    @Override
    public byte[] getResumePdf(Long resumeId) throws IOException {
        log.info("获取简历PDF: ID={}", resumeId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    log.warn("简历不存在: ID={}", resumeId);
                    return new NoSuchElementException("找不到指定ID的简历");
                });
        
        String attachmentUrl = resume.getAttachmentUrl();
        if (attachmentUrl == null || attachmentUrl.isEmpty()) {
            log.warn("简历附件URL为空: ID={}", resumeId);
            throw new NoSuchElementException("简历附件不存在");
        }
        
        // 处理文件路径
        File pdfFile = new File(attachmentUrl);
        
        // 如果文件不存在，尝试使用相对路径
        if (!pdfFile.exists()) {
            log.warn("简历文件不存在(绝对路径): {}", attachmentUrl);
            
            // 获取当前工作目录
            String currentDir = System.getProperty("user.dir");
            
            // 尝试不同的路径组合
            String[] possiblePaths = {
                attachmentUrl,
                currentDir + File.separator + attachmentUrl,
                // 如果attachmentUrl是绝对路径，尝试提取文件名
                currentDir + File.separator + uploadPath + File.separator + new File(attachmentUrl).getName()
            };
            
            boolean fileFound = false;
            for (String path : possiblePaths) {
                pdfFile = new File(path);
                if (pdfFile.exists()) {
                    log.info("找到简历文件: {}", pdfFile.getAbsolutePath());
                    fileFound = true;
                    break;
                }
            }
            
            if (!fileFound) {
                log.error("无法找到简历文件，尝试了多个路径");
                throw new IOException("简历文件不存在，已尝试多个路径");
            }
        }
        
        try {
            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
            log.info("读取简历PDF成功: ID={}, 大小={}bytes", resumeId, pdfBytes.length);
            return pdfBytes;
        } catch (IOException e) {
            log.error("读取简历PDF失败: {}", pdfFile.getAbsolutePath(), e);
            throw new IOException("读取简历文件失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Resume updateStatus(Long resumeId, String status) {
        log.info("更新简历状态: ID={}, 状态={}", resumeId, status);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    log.warn("简历不存在: ID={}", resumeId);
                    return new NoSuchElementException("找不到指定ID的简历");
                });
        
        try {
            Resume.ResumeStatus newStatus = Resume.ResumeStatus.valueOf(status);
            resume.setStatus(newStatus);
            resume.setUpdateTime(LocalDateTime.now());
            
            Resume updatedResume = resumeRepository.save(resume);
            log.info("简历状态更新成功: ID={}, 状态={}", resumeId, newStatus);
            return updatedResume;
        } catch (IllegalArgumentException e) {
            log.error("无效的状态值: {}", status);
            throw new IllegalArgumentException("无效的状态值: " + status);
        } catch (Exception e) {
            log.error("更新简历状态失败: ID={}", resumeId, e);
            throw new RuntimeException("更新简历状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 从PDF文件中提取数据
     */
    private Map<String, String> extractDataFromPDF(File pdfFile) throws IOException {
        log.info("开始解析PDF文件: {}", pdfFile.getPath());
        Map<String, String> extractedData = new HashMap<>();
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            log.info("PDF文件加载成功，页数: {}", document.getNumberOfPages());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            extractedData.put("fullContent", text);
            
            // 尝试使用更高级的Tika解析
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            try (FileInputStream inputStream = new FileInputStream(pdfFile)) {
                PDFParser pdfparser = new PDFParser();
                pdfparser.parse(inputStream, handler, metadata, context);
                log.info("Tika解析成功");
            } catch (TikaException | SAXException e) {
                log.error("使用Tika解析PDF时出错", e);
            }
            
            // 解析文本内容
            Map<String, String> parsedText = parseResumeText(text);
            extractedData.putAll(parsedText);
            log.info("从文本中提取了{}个字段", parsedText.size());
            
            // 添加元数据信息
            for (String name : metadata.names()) {
                extractedData.put("metadata_" + name, metadata.get(name));
            }
            
            return extractedData;
        } catch (IOException e) {
            log.error("解析PDF文件失败: {}", pdfFile.getPath(), e);
            throw new IOException("无法解析PDF文件: " + e.getMessage());
        }
    }
    
    /**
     * 通过正则表达式和启发式方法从简历文本中提取关键信息
     */
    private Map<String, String> parseResumeText(String text) {
        Map<String, String> data = new HashMap<>();
        
        // 姓名处理 - 通常在文档开头附近
        String[] lines = text.split("\n");
        if (lines.length > 0) {
            for (int i = 0; i < Math.min(10, lines.length); i++) {
                String line = lines[i].trim();
                if (line.length() > 0 && line.length() < 20 && !line.contains("@") && !line.contains("履历") && !line.contains("简历")) {
                    data.put("fullName", line);
                    break;
                }
            }
        }
        
        // 提取电子邮件
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            data.put("email", emailMatcher.group());
        }
        
        // 提取电话号码
        Pattern phonePattern = Pattern.compile("(?:1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})");
        Matcher phoneMatcher = phonePattern.matcher(text);
        if (phoneMatcher.find()) {
            data.put("phone", phoneMatcher.group());
        }
        
        // 提取教育信息
        String[] educationLevels = {"博士", "硕士", "本科", "大专", "专科"};
        for (String level : educationLevels) {
            if (text.contains(level)) {
                data.put("education", level);
                break;
            }
        }
        
        // 提取应聘职位
        Pattern positionPattern = Pattern.compile("(?:应聘职位|应聘岗位|应聘)[：:]?\\s*([^\\n\\r]+)");
        Matcher positionMatcher = positionPattern.matcher(text);
        if (positionMatcher.find()) {
            data.put("positionApplied", positionMatcher.group(1).trim());
        }
        
        // 提取专业
        Pattern majorPattern = Pattern.compile("(?:专业)[：:]?\\s*([^\\n\\r,，]+)");
        Matcher majorMatcher = majorPattern.matcher(text);
        if (majorMatcher.find()) {
            data.put("major", majorMatcher.group(1).trim());
        } else if (text.contains("计算机") || text.contains("软件") || text.contains("网络")) {
            // 基于关键词猜测专业
            data.put("major", "计算机相关");
        } else {
            data.put("major", "未知专业");
        }
        
        // 提取毕业年份
        Pattern graduateYearPattern = Pattern.compile("(?:毕业年份|毕业时间)[：:]?\\s*(\\d{4})");
        Matcher graduateYearMatcher = graduateYearPattern.matcher(text);
        if (graduateYearMatcher.find()) {
            data.put("graduateYear", graduateYearMatcher.group(1));
        }
        
        // 提取学校
        Pattern schoolPattern = Pattern.compile("(?:毕业院校|学校|大学|学院)[：:]?\\s*([^\\n\\r,，]+)");
        Matcher schoolMatcher = schoolPattern.matcher(text);
        if (schoolMatcher.find()) {
            data.put("school", schoolMatcher.group(1).trim());
        }
        
        // 提取工作经验
        int workExpStart = text.indexOf("工作经验") != -1 ? text.indexOf("工作经验") : text.indexOf("工作经历");
        int projectStart = text.indexOf("项目经验") != -1 ? text.indexOf("项目经验") : text.indexOf("项目经历");
        if (workExpStart != -1 && projectStart != -1 && projectStart > workExpStart) {
            data.put("workExperience", text.substring(workExpStart, projectStart).trim());
        } else if (workExpStart != -1) {
            int endIdx = Math.min(workExpStart + 1000, text.length());
            data.put("workExperience", text.substring(workExpStart, endIdx).trim());
        }
        
        // 提取项目经验
        int projectEnd = text.indexOf("技能特长") != -1 ? text.indexOf("技能特长") : text.indexOf("教育背景");
        if (projectStart != -1 && projectEnd != -1 && projectEnd > projectStart) {
            data.put("projects", text.substring(projectStart, projectEnd).trim());
        } else if (projectStart != -1) {
            int endIdx = Math.min(projectStart + 1000, text.length());
            data.put("projects", text.substring(projectStart, endIdx).trim());
        }
        
        // 提取技能
        Pattern skillsPattern = Pattern.compile("(?:技能特长|专业技能|技能)[：:]?\\s*([^\\n\\r]+)");
        Matcher skillsMatcher = skillsPattern.matcher(text);
        if (skillsMatcher.find()) {
            data.put("skills", skillsMatcher.group(1).trim());
        }
        
        // 提取获奖情况
        Pattern awardsPattern = Pattern.compile("(?:荣誉奖项|获奖情况|奖项)[：:]?\\s*([^\\n\\r]+)");
        Matcher awardsMatcher = awardsPattern.matcher(text);
        if (awardsMatcher.find()) {
            data.put("awards", awardsMatcher.group(1).trim());
        }
        
        // 提取自我评价
        Pattern selfEvalPattern = Pattern.compile("(?:自我评价|个人评价|自我描述)[：:]?\\s*([^\\n\\r]+)");
        Matcher selfEvalMatcher = selfEvalPattern.matcher(text);
        if (selfEvalMatcher.find()) {
            data.put("selfEvaluation", selfEvalMatcher.group(1).trim());
        }
        
        return data;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
} 