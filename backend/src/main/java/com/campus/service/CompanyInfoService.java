package com.campus.service;

import com.campus.dto.CompanyInfoDTO;
import com.campus.model.Company;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CompanyInfoService {
    /**
     * 获取企业信息
     * @param companyId 企业ID
     * @return 企业信息DTO
     */
    CompanyInfoDTO getCompanyInfo(Long companyId);
    
    /**
     * 更新企业信息
     * @param companyId 企业ID
     * @param companyInfoDTO 企业信息DTO
     * @return 更新后的企业信息
     */
    CompanyInfoDTO updateCompanyInfo(Long companyId, CompanyInfoDTO companyInfoDTO);
    
    /**
     * 上传企业Logo
     * @param companyId 企业ID
     * @param file Logo文件
     * @return Logo的URL
     * @throws IOException 文件处理异常
     */
    String uploadCompanyLogo(Long companyId, MultipartFile file) throws IOException;
    
    /**
     * 创建企业信息
     * @param userId 用户ID
     * @param companyInfoDTO 企业信息DTO
     * @return 创建后的企业信息
     */
    CompanyInfoDTO createCompanyInfo(Long userId, CompanyInfoDTO companyInfoDTO);
} 