package com.campus.repository;

import com.campus.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JobRepository extends JpaRepository<Job, Long> {
    
    Page<Job> findByTitleContainingOrDescriptionContaining(
        String titleKeyword, String descriptionKeyword, Pageable pageable);
    
    @Modifying
    @Query("UPDATE Job j SET j.viewCount = j.viewCount + 1 WHERE j.id = ?1")
    void incrementViewCount(Long jobId);
    
    @Modifying
    @Query("UPDATE Job j SET j.applyCount = j.applyCount + 1 WHERE j.id = ?1")
    void incrementApplyCount(Long jobId);
    
    Page<Job> findByCompanyId(Long companyId, Pageable pageable);
    
    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);
} 