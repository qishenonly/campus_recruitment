package com.campus.repository;

import com.campus.model.JobApplication;
import com.campus.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    
    // 查找学生的所有申请
    Page<JobApplication> findByStudentId(Long studentId, Pageable pageable);
    
    // 查找某个职位的所有申请
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
    
    // 查找企业收到的所有职位申请
    @Query("SELECT ja FROM JobApplication ja JOIN Job j ON ja.jobId = j.id WHERE j.companyId = :companyId")
    Page<JobApplication> findByJobCompanyId(Long companyId, Pageable pageable);
    
    // 检查学生是否已申请某职位
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
    
    /**
     * 查询公司收到的所有简历
     * @param companyId 公司ID
     * @param pageable 分页参数
     * @return 简历分页列表
     */
    @Query("SELECT DISTINCT r FROM Resume r JOIN JobApplication ja ON r.id = ja.resumeId " +
           "JOIN Job j ON ja.jobId = j.id " +
           "WHERE j.companyId = ?1 " +
           "ORDER BY r.updateTime DESC")
    Page<Resume> findResumesByCompanyId(Long companyId, Pageable pageable);
    
    /**
     * 检查简历是否属于特定公司
     * @param resumeId 简历ID
     * @param companyId 公司ID
     * @return 是否属于该公司
     */
    @Query("SELECT COUNT(ja) > 0 FROM JobApplication ja " +
           "JOIN Job j ON ja.jobId = j.id " +
           "WHERE ja.resumeId = ?1 AND j.companyId = ?2")
    boolean existsByResumeIdAndCompanyId(Long resumeId, Long companyId);
    
    /**
     * 统计创建时间在指定日期之前的职位申请数量
     * @param dateTime 指定日期
     * @return 职位申请数量
     */
    long countByCreateTimeBefore(LocalDateTime dateTime);
    
    /**
     * 统计创建时间在指定日期范围内的职位申请数量
     * @param startDateTime 开始日期
     * @param endDateTime 结束日期
     * @return 职位申请数量
     */
    long countByCreateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
} 