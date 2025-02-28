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
} 