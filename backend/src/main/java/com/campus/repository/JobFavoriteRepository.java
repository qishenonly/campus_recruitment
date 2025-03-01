package com.campus.repository;

import com.campus.model.JobFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobFavoriteRepository extends JpaRepository<JobFavorite, Long> {
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
    void deleteByStudentIdAndJobId(Long studentId, Long jobId);
    Page<JobFavorite> findByStudentId(Long studentId, Pageable pageable);
} 