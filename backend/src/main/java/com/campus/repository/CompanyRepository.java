package com.campus.repository;

import com.campus.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    // 基础的CRUD方法由JpaRepository提供
    
    // 可以添加自定义查询方法
    Company findByCompanyName(String companyName);
    
    Page<Company> findAll(Pageable pageable);
    
    Page<Company> findByCompanyNameContainingOrIndustryContaining(String companyName, String industry, Pageable pageable);
    
    /**
     * 统计创建时间在指定日期之前的企业数量
     * @param dateTime 指定日期
     * @return 企业数量
     */
    @Query("SELECT COUNT(c) FROM Company c JOIN c.user u WHERE u.createTime < :dateTime")
    long countByCreateTimeBefore(@Param("dateTime") LocalDateTime dateTime);
    
    /**
     * 统计创建时间在指定日期范围内的企业数量
     * @param startDateTime 开始日期
     * @param endDateTime 结束日期
     * @return 企业数量
     */
    @Query("SELECT COUNT(c) FROM Company c JOIN c.user u WHERE u.createTime >= :startDateTime AND u.createTime < :endDateTime")
    long countByCreateTimeBetween(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
} 