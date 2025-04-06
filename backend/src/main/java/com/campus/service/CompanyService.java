package com.campus.service;

import com.campus.model.Company;
import com.campus.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public Page<Company> findAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    public Page<Company> searchCompanies(String keyword, Pageable pageable) {
        return companyRepository.findByCompanyNameContainingOrIndustryContaining(keyword, keyword, pageable);
    }
    
    // 根据用户ID查找企业ID
    public Long findCompanyIdByUserId(Long userId) {
        Company company = companyRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("企业信息不存在"));
        return company.getId();
    }
    
    // 保存或更新企业信息
    public Company save(Company company) {
        return companyRepository.save(company);
    }

    /**
     * 根据公司ID查询公司名称
     */
    public String findCompanyNameById(Long companyId) {
        return companyRepository.findById(companyId)
                .map(Company::getCompanyName)
                .orElse("未知公司");
    }
} 