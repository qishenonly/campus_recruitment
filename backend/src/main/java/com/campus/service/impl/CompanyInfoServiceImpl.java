package com.campus.service.impl;

import com.campus.dto.CompanyInfoDTO;
import com.campus.model.Company;
import com.campus.model.TeamMember;
import com.campus.repository.CompanyRepository;
import com.campus.repository.TeamMemberRepository;
import com.campus.service.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyInfoServiceImpl implements CompanyInfoService {

    private final CompanyRepository companyRepository;
    private final TeamMemberRepository teamMemberRepository;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public CompanyInfoDTO getCompanyInfo(Long userId) {
        log.info("获取用户ID={}的企业信息", userId);
        
        // 通过用户ID查找对应的团队成员
        TeamMember teamMember = teamMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("找不到该用户关联的企业信息"));
        
        // 获取团队成员所属的企业ID
        Long companyId = teamMember.getCompanyId();
        log.info("找到用户关联的企业ID={}", companyId);
        
        // 根据企业ID获取企业信息
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        return convertToDTO(company);
    }

    @Override
    public CompanyInfoDTO updateCompanyInfo(Long userId, CompanyInfoDTO companyInfoDTO) {
        log.info("更新用户ID={}的企业信息", userId);
        
        // 通过用户ID查找对应的团队成员
        TeamMember teamMember = teamMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("找不到该用户关联的企业信息"));
        
        // 获取团队成员所属的企业ID
        Long companyId = teamMember.getCompanyId();
        log.info("找到用户关联的企业ID={}", companyId);
        
        // 根据企业ID获取企业对象
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        // 更新公司信息
        company.setCompanyName(companyInfoDTO.getName());
        company.setDescription(companyInfoDTO.getDescription());
        
        // 如果city是列表，转换为字符串
        if (companyInfoDTO.getCity() != null && !companyInfoDTO.getCity().isEmpty()) {
            company.setCity(companyInfoDTO.getCity().get(0)); // 这里简化处理，只取第一级城市
        }
        
        company.setAddress(companyInfoDTO.getAddress());
        company.setScale(companyInfoDTO.getSize());
        company.setIndustry(companyInfoDTO.getIndustry());
        company.setFinancingStage(companyInfoDTO.getFinancingStage());
        company.setWebsite(companyInfoDTO.getWebsite());
        company.setContactPerson(companyInfoDTO.getContactPerson());
        company.setContactPosition(companyInfoDTO.getContactPosition());
        
        Company updatedCompany = companyRepository.save(company);
        log.info("企业信息更新成功");
        
        return convertToDTO(updatedCompany);
    }

    @Override
    public String uploadCompanyLogo(Long userId, MultipartFile file) throws IOException {
        log.info("上传用户ID={}的企业Logo", userId);
        
        // 通过用户ID查找对应的团队成员
        TeamMember teamMember = teamMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("找不到该用户关联的企业信息"));
        
        // 获取团队成员所属的企业ID
        Long companyId = teamMember.getCompanyId();
        log.info("找到用户关联的企业ID={}", companyId);
        
        // 检查公司是否存在
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("只支持jpg和png格式的图片");
        }
        
        // 确保目录存在
        String logoDir = uploadDir + "/company/logo";
        File directory = new File(logoDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("无法创建上传目录");
            }
        }
        
        // 生成唯一文件名
        String extension = contentType.equals("image/jpeg") ? ".jpg" : ".png";
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = Paths.get(logoDir, fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 构建访问URL
        String logoUrl = accessPath + "/company/logo/" + fileName;
        
        // 更新公司logo
        company.setLogo(logoUrl);
        companyRepository.save(company);
        
        log.info("企业Logo上传成功，URL: {}", logoUrl);
        return logoUrl;
    }
    
    /**
     * 将公司实体转换为DTO
     */
    private CompanyInfoDTO convertToDTO(Company company) {
        CompanyInfoDTO dto = new CompanyInfoDTO();
        dto.setId(company.getId());
        dto.setName(company.getCompanyName());
        dto.setDescription(company.getDescription());
        dto.setLogo(company.getLogo());
        
        // 城市处理
        if (company.getCity() != null) {
            dto.setCity(Arrays.asList(company.getCity()));
        }
        
        dto.setAddress(company.getAddress());
        dto.setSize(company.getScale());
        dto.setIndustry(company.getIndustry());
        dto.setFinancingStage(company.getFinancingStage());
        dto.setWebsite(company.getWebsite());
        dto.setContactPerson(company.getContactPerson());
        dto.setContactPosition(company.getContactPosition());
        
        return dto;
    }
} 