package com.campus.service;

import com.campus.model.Company;
import com.campus.model.TeamMember;
import com.campus.repository.CompanyRepository;
import com.campus.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public Page<com.campus.model.Company> findAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    public Page<com.campus.model.Company> searchCompanies(String keyword, Pageable pageable) {
        return companyRepository.findByCompanyNameContainingOrIndustryContaining(keyword, keyword, pageable);
    }
    
    // 根据用户ID查找企业ID
    public Long findCompanyIdByUserId(Long userId) {
        // 查找用户所属的团队成员记录
        TeamMember teamMember = teamMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("找不到该用户关联的企业信息"));
        
        // 获取关联的企业ID
        return teamMember.getCompanyId();
    }
    
    // 保存或更新企业信息
    public com.campus.model.Company save(com.campus.model.Company company) {
        return companyRepository.save(company);
    }

    /**
     * 根据公司ID查询公司名称
     */
    public String findCompanyNameById(Long companyId) {
        return companyRepository.findById(companyId)
                .map(com.campus.model.Company::getCompanyName)
                .orElse("未知公司");
    }
} 