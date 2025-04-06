package com.campus.service;

import com.campus.model.Interview;
import com.campus.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;
    
    public Interview createInterview(Interview interview) {
        return interviewRepository.save(interview);
    }

    public Optional<Interview> findById(Long id) {
        return interviewRepository.findById(id);
    }

    public Page<Interview> findByCompanyId(Long companyId, Pageable pageable) {
        return interviewRepository.findByCompanyId(companyId, pageable);
    }

    public Interview updateInterview(Interview interview) {
        return interviewRepository.save(interview);
    }

    public void updateStatus(Long interviewId, Interview.InterviewStatus status) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("面试不存在"));
        
        interview.setStatus(status);
        interview.setUpdateTime(LocalDateTime.now());
        
        interviewRepository.save(interview);
    }

    public void deleteInterview(Long id) {
        interviewRepository.deleteById(id);
    }

    public void updateInterviewStatus(Long interviewId, Interview.InterviewStatus status, String token) {
        // 可以添加token验证逻辑，确保是合法的请求
        // 如果token为空，可能是通过邮件链接点击的
        
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("面试不存在"));
        
        interview.setStatus(status);
        interview.setUpdateTime(LocalDateTime.now());
        
        interviewRepository.save(interview);
    }
} 