package com.campus.service;

import com.campus.model.Job;
import com.campus.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;
    
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
        jobRepository.incrementApplyCount(jobId);
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
} 