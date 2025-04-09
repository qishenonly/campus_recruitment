package com.campus.controller;

import com.campus.dto.CompanyInfoDTO;
import com.campus.service.CompanyInfoService;
import com.campus.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company")
public class CompanyInfoController {

    private final CompanyInfoService companyInfoService;

    /**
     * 获取企业信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getCompanyInfo(@RequestHeader("Authorization") String token) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业信息
            CompanyInfoDTO companyInfo = companyInfoService.getCompanyInfo(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取企业信息成功");
            response.put("data", companyInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取企业信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取企业信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 更新企业信息
     */
    @PutMapping("/info")
    public ResponseEntity<?> updateCompanyInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody CompanyInfoDTO companyInfoDTO) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 更新企业信息
            CompanyInfoDTO updatedInfo = companyInfoService.updateCompanyInfo(userId, companyInfoDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新企业信息成功");
            response.put("data", updatedInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新企业信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新企业信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 上传企业Logo
     */
    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCompanyLogo(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 上传Logo
            String logoUrl = companyInfoService.uploadCompanyLogo(userId, file);
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", logoUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Logo上传成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Logo上传失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Logo上传失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 创建企业信息
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCompanyInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> companyData) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            log.info("创建企业信息，用户ID={}, 数据={}", userId, companyData);
            
            // 构建企业DTO对象
            CompanyInfoDTO companyInfoDTO = new CompanyInfoDTO();
            companyInfoDTO.setName((String) companyData.get("companyName"));
            companyInfoDTO.setIndustry((String) companyData.get("industry"));
            companyInfoDTO.setSize((String) companyData.get("scale"));
            companyInfoDTO.setDescription((String) companyData.get("description"));
            companyInfoDTO.setWebsite((String) companyData.get("website"));
            
            // 处理地址相关信息
            if (companyData.get("location") != null) {
                companyInfoDTO.setAddress((String) companyData.get("location"));
            }
            
            // 添加联系人信息
            companyInfoDTO.setContactPerson((String) companyData.get("memberName"));
            companyInfoDTO.setContactPosition((String) companyData.get("position"));
            companyInfoDTO.setEmail((String) companyData.get("memberEmail"));
            companyInfoDTO.setPhone((String) companyData.get("memberPhone"));
            
            // 调用服务创建企业信息
            CompanyInfoDTO createdCompany = companyInfoService.createCompanyInfo(userId, companyInfoDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "企业信息创建成功");
            response.put("data", createdCompany);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建企业信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建企业信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}