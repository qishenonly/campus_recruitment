package com.campus.controller;

import com.campus.dto.CompanyDTO;
import com.campus.dto.ResponseDTO;
import com.campus.model.Company;
import com.campus.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public ResponseEntity<?> getAllCompanies(Pageable pageable) {
        try {
            Page<Company> companyPage = companyService.findAllCompanies(pageable);
            
            // 转换为DTO，避免循环引用
            Page<CompanyDTO> companyDTOPage = companyPage.map(this::convertToDTO);
            
            return ResponseEntity.ok(companyDTOPage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.error("获取企业列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCompanies(@RequestParam String keyword, Pageable pageable) {
        try {
            Page<Company> companyPage = companyService.searchCompanies(keyword, pageable);
            
            // 转换为DTO，避免循环引用
            Page<CompanyDTO> companyDTOPage = companyPage.map(this::convertToDTO);
            
            return ResponseEntity.ok(companyDTOPage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.error("搜索企业失败: " + e.getMessage()));
        }
    }
    
    /**
     * 将Company实体转换为DTO
     */
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        if (company == null) {
            return dto;
        }
        
        dto.setId(company.getId());
        dto.setName(company.getCompanyName());
        dto.setIndustry(company.getIndustry());
        dto.setScale(company.getScale());
        dto.setLocation(company.getCity());
        dto.setContactPerson(company.getContactPerson());
        dto.setDescription(company.getDescription());
        dto.setVerified(company.getVerified());
        dto.setLogo(company.getLogo());
        
        return dto;
    }
} 