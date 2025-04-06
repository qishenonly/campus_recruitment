package com.campus.service;

import com.campus.model.Resume;
import com.campus.repository.JobApplicationRepository;
import com.campus.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CompanyResumeService {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private CompanyService companyService;

    /**
     * 获取公司收到的所有简历
     * @param companyId 公司ID
     * @param pageable 分页参数
     * @return 简历分页列表
     */
    @Transactional(readOnly = true)
    public Page<Resume> getCompanyResumes(Long companyId, Pageable pageable) {
        log.info("获取公司ID={}的简历列表", companyId);
        
        Page<Resume> resumesPage = jobApplicationRepository.findResumesByCompanyId(companyId, pageable);
        
        // 处理简历中的空字段
        List<Resume> processedResumes = new ArrayList<>();
        for (Resume resume : resumesPage.getContent()) {
            Resume processedResume = processResume(resume);
            processedResumes.add(processedResume);
        }
        
        log.info("成功获取到{}份简历", processedResumes.size());
        return new PageImpl<>(processedResumes, pageable, resumesPage.getTotalElements());
    }
    
    /**
     * 获取简历详情
     * @param resumeId 简历ID
     * @param companyId 公司ID
     * @return 处理后的简历详情
     * @throws NoSuchElementException 如果简历不存在或不属于该公司
     */
    @Transactional(readOnly = true)
    public Resume getResumeDetail(Long resumeId, Long companyId) {
        log.info("获取简历详情: resumeId={}, companyId={}", resumeId, companyId);
        
        // 检查简历是否属于该公司
        boolean isCompanyResume = jobApplicationRepository.existsByResumeIdAndCompanyId(resumeId, companyId);
        if (!isCompanyResume) {
            log.warn("简历不属于该公司: resumeId={}, companyId={}", resumeId, companyId);
            throw new NoSuchElementException("该简历不属于您的公司或不存在");
        }
        
        // 获取简历
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    log.warn("简历不存在: resumeId={}", resumeId);
                    return new NoSuchElementException("简历不存在");
                });
        
        // 处理简历数据
        Resume processedResume = processResume(resume);
        
        log.info("成功获取简历详情: resumeId={}", resumeId);
        return processedResume;
    }
    
    /**
     * 处理简历数据，确保所有字段有合理的值
     * @param resume 原始简历
     * @return 处理后的简历
     */
    private Resume processResume(Resume resume) {
        log.debug("处理简历: ID={}, 名称={}", resume.getId(), resume.getName());
        
        // 如果状态为null，设置默认值
        if (resume.getStatus() == null) {
            resume.setStatus(Resume.ResumeStatus.待处理);
            resume = resumeRepository.save(resume);
            log.debug("简历状态为null，已设置为默认值: {}", Resume.ResumeStatus.待处理);
        }
        
        // 如果提交时间为null，使用创建时间
        if (resume.getSubmitTime() == null) {
            resume.setSubmitTime(resume.getCreateTime());
            resume = resumeRepository.save(resume);
            log.debug("提交时间为null，已使用创建时间代替");
        }
        
        // 处理联系方式字段
        if (resume.getPhone() == null && resume.getContent() != null) {
            String phone = extractPhone(resume.getContent());
            if (phone != null) {
                resume.setPhone(phone);
                log.debug("从内容中提取到电话: {}", phone);
            }
        }
        
        // 处理邮箱字段
        if (resume.getEmail() == null && resume.getContent() != null) {
            String email = extractEmail(resume.getContent());
            if (email != null) {
                resume.setEmail(email);
                log.debug("从内容中提取到邮箱: {}", email);
            }
        }
        
        // 处理学校和专业字段
        if ((resume.getSchool() == null || resume.getMajor() == null) && resume.getContent() != null) {
            String content = resume.getContent();
            
            // 提取学校信息
            if (resume.getSchool() == null) {
                String school = extractSchool(content);
                if (school != null) {
                    resume.setSchool(school);
                    log.debug("从内容中提取到学校: {}", school);
                }
            }
            
            // 提取专业信息
            if (resume.getMajor() == null) {
                String major = extractMajor(content);
                if (major != null) {
                    resume.setMajor(major);
                    log.debug("从内容中提取到专业: {}", major);
                }
            }
        }
        
        // 如果有修改字段，保存更新
        if (resume.getPhone() != null || resume.getEmail() != null || 
            resume.getSchool() != null || resume.getMajor() != null) {
            resume.setUpdateTime(LocalDateTime.now());
            resume = resumeRepository.save(resume);
            log.debug("简历字段已更新并保存");
        }
        
        return resume;
    }
    
    /**
     * 从文本中提取电话号码
     */
    private String extractPhone(String text) {
        if (text == null) return null;
        
        // 手机号码正则表达式
        Pattern pattern = Pattern.compile("(?:1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    /**
     * 从文本中提取邮箱
     */
    private String extractEmail(String text) {
        if (text == null) return null;
        
        // 邮箱正则表达式
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    /**
     * 从文本中提取学校
     */
    private String extractSchool(String text) {
        if (text == null) return null;
        
        // 学校正则表达式，匹配常见格式如"xx大学"、"xx学院"
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]+(?:大学|学院|学校))");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    /**
     * 从文本中提取专业
     */
    private String extractMajor(String text) {
        if (text == null) return null;
        
        // 专业正则表达式，尝试匹配"专业：xxx"或"xxx专业"格式
        Pattern pattern = Pattern.compile("专业[：:]?\\s*([^\\n\\r,，]+)|([\\u4e00-\\u9fa5]+(?:工程|技术|科学|学))(?:专业)?");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return (matcher.group(1) != null) ? matcher.group(1).trim() : 
                   (matcher.group(2) != null) ? matcher.group(2).trim() : null;
        }
        return null;
    }
} 