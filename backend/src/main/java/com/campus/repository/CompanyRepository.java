package com.campus.repository;

import com.campus.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    // 基础的CRUD方法由JpaRepository提供
    
    // 可以添加自定义查询方法
    Company findByCompanyName(String companyName);
    
    Page<Company> findAll(Pageable pageable);
    
    Page<Company> findByCompanyNameContainingOrIndustryContaining(String companyName, String industry, Pageable pageable);
} 