package com.campus.service;

import com.campus.model.JobApplication;
import com.campus.model.Conversation;
import com.campus.model.Message;
import com.campus.dto.JobDTO;
import com.campus.repository.JobApplicationRepository;
import com.campus.repository.ConversationRepository;
import com.campus.repository.JobRepository;
import com.campus.repository.ResumeRepository;
import com.campus.service.JobService;
import com.campus.service.ConversationService;
import com.campus.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JobApplicationService {
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    public boolean hasApplied(Long studentId, Long jobId) {
        return jobApplicationRepository.existsByStudentIdAndJobId(studentId, jobId);
    }
    
    @Transactional
    public Map<String, Object> apply(Long studentId, Long jobId, Long resumeId, String coverLetter) {
        // 检查是否已经投递过该职位
        if (hasApplied(studentId, jobId)) {
            throw new RuntimeException("您已经投递过该职位，不能重复投递");
        }
        
        // 检查简历是否存在
        if (!resumeRepository.existsById(resumeId)) {
            throw new RuntimeException("所选简历不存在，请先上传简历");
        }
        
        // 检查简历是否属于该学生（防止使用他人简历）
        if (!resumeRepository.findById(resumeId).get().getStudentId().equals(studentId)) {
            throw new RuntimeException("无法使用不属于您的简历");
        }
        
        // 检查职位是否存在
        if (!jobRepository.existsById(jobId)) {
            throw new RuntimeException("所选职位不存在");
        }
        
        try {
            // 获取职位信息，包括发布者ID
            JobDTO jobDTO = jobService.findByIdWithCompany(jobId)
                    .orElseThrow(() -> new RuntimeException("职位不存在"));
            
            // 创建投递记录
            JobApplication application = new JobApplication();
            application.setStudentId(studentId);
            application.setJobId(jobId);
            application.setResumeId(resumeId);
            application.setCoverLetter(coverLetter);
            application.setStatus(JobApplication.ApplicationStatus.PENDING);
            application.setCreateTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());
            
            JobApplication savedApplication = jobApplicationRepository.save(application);
            
            // 更新职位申请数
            jobService.incrementApplyCount(jobId);

            // 创建会话
            Conversation conversation = new Conversation();
            conversation.setApplicationId(savedApplication.getId());
            conversation.setStudentId(studentId);
            conversation.setCompanyId(jobDTO.getCompanyId());  // 使用公司ID
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
            
            Conversation savedConversation = conversationService.save(conversation);

            // 创建第一条消息（求职信）
            Message message = new Message();
            message.setConversationId(savedConversation.getId());
            message.setSenderId(studentId);  // 发送者是学生
            message.setContent(coverLetter);
            message.setCreateTime(LocalDateTime.now());
            message.setIsRead(false);
            
            messageService.save(message);

            // 返回包含所有必要信息的Map
            Map<String, Object> result = new HashMap<>();
            result.put("application", savedApplication);
            result.put("conversation", savedConversation);
            result.put("companyId", jobDTO.getCompanyId());
            result.put("companyName", jobDTO.getCompanyName());
            result.put("conversationId", savedConversation.getId());
            
            return result;
        } catch (Exception e) {
            log.error("职位申请失败", e);
            throw new RuntimeException("职位申请失败：" + e.getMessage());
        }
    }
    
    public Page<JobApplication> getStudentApplications(Long studentId, Pageable pageable) {
        return jobApplicationRepository.findByStudentId(studentId, pageable);
    }
    
    public Page<JobApplication> getJobApplications(Long jobId, Pageable pageable) {
        return jobApplicationRepository.findByJobId(jobId, pageable);
    }
    
    @Transactional
    public JobApplication updateStatus(Long id, JobApplication.ApplicationStatus status) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("投递记录不存在"));
        
        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        
        return jobApplicationRepository.save(application);
    }

    /**
     * 根据ID查找JobApplication
     * @param id 应用ID
     * @return JobApplication对象，如果不存在则返回null
     */
    public JobApplication findById(Long id) {
        return jobApplicationRepository.findById(id).orElse(null);
    }
}