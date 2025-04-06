package com.campus.repository;

import com.campus.model.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    @Query("SELECT i FROM Interview i JOIN Job j ON i.jobId = j.id WHERE j.companyId = :companyId")
    Page<Interview> findByCompanyId(Long companyId, Pageable pageable);
} 