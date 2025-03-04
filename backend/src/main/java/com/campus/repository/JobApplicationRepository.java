package com.campus.repository;

import com.campus.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
    Page<JobApplication> findByStudentId(Long studentId, Pageable pageable);
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
} 