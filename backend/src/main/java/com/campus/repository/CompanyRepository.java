package com.campus.repository;

import com.campus.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    // 基础的CRUD方法由JpaRepository提供
    
    // 可以添加自定义查询方法
    Company findByCompanyName(String companyName);
} 