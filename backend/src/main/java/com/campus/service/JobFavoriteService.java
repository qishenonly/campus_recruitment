package com.campus.service;

import com.campus.model.JobFavorite;
import com.campus.repository.JobFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobFavoriteService {
    
    @Autowired
    private JobFavoriteRepository jobFavoriteRepository;
    
    public boolean isFavorited(Long studentId, Long jobId) {
        return jobFavoriteRepository.existsByStudentIdAndJobId(studentId, jobId);
    }
    
    @Transactional
    public void addFavorite(Long studentId, Long jobId) {
        if (!isFavorited(studentId, jobId)) {
            JobFavorite favorite = new JobFavorite();
            favorite.setStudentId(studentId);
            favorite.setJobId(jobId);
            favorite.setCreateTime(LocalDateTime.now());
            jobFavoriteRepository.save(favorite);
        }
    }
    
    @Transactional
    public void removeFavorite(Long studentId, Long jobId) {
        jobFavoriteRepository.deleteByStudentIdAndJobId(studentId, jobId);
    }
    
    public Page<JobFavorite> getFavorites(Long studentId, Pageable pageable) {
        return jobFavoriteRepository.findByStudentId(studentId, pageable);
    }
} 