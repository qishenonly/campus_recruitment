package com.campus.service;

import com.campus.model.Job;
import com.campus.model.Company;
import com.campus.model.User;
import com.campus.model.JobApplication;
import com.campus.repository.JobRepository;
import com.campus.repository.CompanyRepository;
import com.campus.repository.UserRepository;
import com.campus.repository.JobApplicationRepository;
import com.campus.dto.JobDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    public Page<Job> findAll(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }
    
    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }
    
    public Job save(Job job) {
        if (job.getId() == null) {
            job.setPublishDate(LocalDateTime.now());
            job.setViewCount(0);
            job.setApplyCount(0);
            job.setStatus(Job.JobStatus.PUBLISHED);
        }
        return jobRepository.save(job);
    }
    
    public Page<Job> search(String keyword, String location, String education, 
                          Job.PositionType positionType, String salary, Pageable pageable) {
        // TODO: 实现更复杂的搜索逻辑
        return jobRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword, pageable);
    }
    
    @Transactional
    public void incrementViewCount(Long jobId) {
        jobRepository.incrementViewCount(jobId);
    }
    
    @Transactional
    public void incrementApplyCount(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("职位不存在"));
        job.setApplyCount(job.getApplyCount() + 1);
        jobRepository.save(job);
    }
    
    public void updateStatus(Long jobId, Job.JobStatus status) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        jobRepository.save(job);
    }

    // 添加获取企业职位列表的方法
    public Page<Job> findByCompanyId(Long companyId, Pageable pageable) {
        return jobRepository.findByCompanyId(companyId, pageable);
    }

    public JobDTO convertToDTO(Job job) {
        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setCompanyId(job.getPublisherId());  // 发布者的用户ID
        dto.setCompanyEntityId(job.getCompanyId()); // 实际公司ID
        BeanUtils.copyProperties(job, dto);
        
        // 获取公司信息
        Company company = companyRepository.findById(job.getCompanyId())
                .orElseThrow(() -> new RuntimeException("公司不存在"));
        
        dto.setCompanyName(company.getCompanyName());
        dto.setIndustry(company.getIndustry());
        dto.setCompanyLogo(company.getLogo());
        dto.setCompanyScale(company.getScale());
        dto.setCompanyVerified(company.getVerified());

        // 获取发布者信息
        User publisher = userRepository.findById(job.getPublisherId())
                .orElseThrow(() -> new RuntimeException("发布者不存在"));
        
        dto.setPublisherId(publisher.getId());
        dto.setPublisherName(job.getPublisherName());
        dto.setPublisherPosition(job.getPublisherPosition());  // 从公司信息中获取职位
        
        return dto;
    }

    public Page<JobDTO> findAllWithCompany(Pageable pageable) {
        Page<Job> jobs = jobRepository.findAll(pageable);
        return jobs.map(job -> convertToDTO(job));
    }
    
    public Optional<JobDTO> findByIdWithCompany(Long id) {
        return jobRepository.findById(id)
            .map(job -> {
                JobDTO dto = convertToDTO(job);
                Company company = companyRepository.findById(job.getCompanyId())
                    .orElse(null);
                if (company != null) {
                    dto.setCompanyName(company.getCompanyName());
                    dto.setIndustry(company.getIndustry());
                    dto.setCompanyLogo(company.getLogo());
                    dto.setCompanyScale(company.getScale());
                    dto.setCompanyVerified(company.getVerified());
                }
                return dto;
            });
    }
    
    public Page<JobDTO> searchWithCompany(String keyword, String location, 
            String education, Job.PositionType positionType, 
            String salary, Pageable pageable) {
        // 将枚举转换为字符串
        String positionTypeStr = positionType != null ? positionType.name() : null;
        
        log.info("Searching with params: keyword={}, location={}, education={}, positionType={}, salary={}", 
                keyword, location, education, positionTypeStr, salary);
                
        Page<Job> jobPage = jobRepository.searchJobs(
            keyword, location, education, positionTypeStr, salary, pageable);
            
        return jobPage.map(this::convertToDTO);
    }
    
    public Page<JobDTO> findByCompanyIdWithCompany(Long companyId, Pageable pageable) {
        return jobRepository.findByCompanyId(companyId, pageable).map(this::convertToDTO);
    }

    public Long getCompanyIdByJobId(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("职位不存在"));
        return job.getCompanyId();
    }

    // 查找企业收到的所有职位申请
    public Page<JobApplication> findApplicationsByCompanyId(Long companyId, Pageable pageable) {
        return jobApplicationRepository.findByJobCompanyId(companyId, pageable);
    }
    
    // 更新申请状态
    @Transactional
    public void updateApplicationStatus(Long applicationId, Long companyId, String status) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("申请记录不存在"));
        
        // 验证该申请是否属于该企业的职位
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new RuntimeException("职位不存在"));
        
        if (!job.getCompanyId().equals(companyId)) {
            throw new RuntimeException("无权操作此申请");
        }
        
        // 更新状态
        application.setStatus(JobApplication.ApplicationStatus.valueOf(status));
        application.setUpdateTime(LocalDateTime.now());
        jobApplicationRepository.save(application);
    }

    /**
     * 获取指定企业的职位数量
     * @param companyId 企业ID
     * @return 职位数量
     */
    public long countByCompanyId(Long companyId) {
        return jobRepository.countByCompanyId(companyId);
    }

    /**
     * 获取指定企业收到的简历数量
     * @param companyId 企业ID
     * @return 简历数量
     */
    public long countApplicationsByCompanyId(Long companyId) {
        return jobApplicationRepository.countByJobCompanyId(companyId);
    }

    /**
     * 获取指定企业的面试数量
     * @param companyId 企业ID
     * @param status 面试状态
     * @return 面试数量
     */
    public long countInterviewsByCompanyIdAndStatus(Long companyId, String status) {
        // 此处需要实际的Interview数据模型和Repository
        // 暂时返回0，需要根据实际项目情况实现
        return 0;
    }
} 