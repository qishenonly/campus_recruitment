package com.campus.repository;

import com.campus.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    // 返回学生的所有简历
    List<Resume> findByStudentId(Long studentId);
    
    // 查询学生最新提交的简历
    @Query("SELECT r FROM Resume r WHERE r.studentId = ?1 ORDER BY r.submitTime DESC")
    List<Resume> findByStudentIdOrderBySubmitTimeDesc(Long studentId);
    
    // 通过学生ID和简历状态查询
    List<Resume> findByStudentIdAndStatus(Long studentId, Resume.ResumeStatus status);
} 