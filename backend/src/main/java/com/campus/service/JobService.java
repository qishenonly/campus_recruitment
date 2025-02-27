package com.campus.service;

import com.campus.model.Job;
import com.campus.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;
    
    public List<Job> findAll() {
        return jobRepository.findAll();
    }
    
    public Job save(Job job) {
        if (job.getId() == null) {
            job.setCreateTime(LocalDateTime.now());
        }
        job.setUpdateTime(LocalDateTime.now());
        return jobRepository.save(job);
    }
} 