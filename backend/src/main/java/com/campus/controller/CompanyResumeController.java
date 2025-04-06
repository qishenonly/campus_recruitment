package com.campus.controller;

import com.campus.model.Resume;
import com.campus.service.CompanyResumeService;
import com.campus.service.CompanyService;
import com.campus.service.ResumeParseService;
import com.campus.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/company/resumes")
public class CompanyResumeController {

    @Autowired
    private CompanyResumeService companyResumeService;

    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private ResumeParseService resumeParseService;

    /**
     * 获取公司收到的所有简历
     * @param token 认证令牌
     * @param page 页码
     * @param size 每页大小
     * @return 简历列表
     */
    @GetMapping
    public ResponseEntity<?> getCompanyResumes(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取简历列表
            Page<Resume> resumes = companyResumeService.getCompanyResumes(
                companyId, 
                PageRequest.of(page, size)
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", resumes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取简历列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取简历列表失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取简历详情
     * @param token 认证令牌
     * @param resumeId 简历ID
     * @return 简历详情
     */
    @GetMapping("/{resumeId}")
    public ResponseEntity<?> getResumeDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long resumeId) {
        try {
            log.info("获取简历详情: resumeId={}", resumeId);
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取简历详情
            Resume resume = companyResumeService.getResumeDetail(resumeId, companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", resume);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("获取简历详情失败: resumeId={}, {}", resumeId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "简历不存在：" + e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("获取简历详情失败: resumeId={}", resumeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取简历详情失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新简历状态
     * @param token 认证令牌
     * @param resumeId 简历ID
     * @param requestBody 请求体，包含状态信息
     * @return 更新结果
     */
    @PutMapping("/{resumeId}/status")
    public ResponseEntity<?> updateResumeStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long resumeId,
            @RequestBody Map<String, String> requestBody) {
        try {
            log.info("更新简历状态: resumeId={}, status={}", resumeId, requestBody.get("status"));
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 更新简历状态
            String status = requestBody.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "状态不能为空"
                ));
            }
            
            Resume updatedResume = resumeParseService.updateStatus(resumeId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "状态更新成功");
            response.put("data", updatedResume);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            log.warn("更新简历状态失败: resumeId={}, {}", resumeId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "简历不存在：" + e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("更新简历状态失败: resumeId={}", resumeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新简历状态失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 下载简历PDF
     * @param token 认证令牌
     * @param resumeId 简历ID
     * @return PDF文件
     */
    @GetMapping("/{resumeId}/download")
    public ResponseEntity<?> downloadResume(
            @RequestHeader("Authorization") String token,
            @PathVariable Long resumeId) {
        try {
            log.info("下载简历: resumeId={}", resumeId);
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 先验证权限
            companyResumeService.getResumeDetail(resumeId, companyId);
            
            // 获取PDF文件
            byte[] pdfBytes = resumeParseService.getResumePdf(resumeId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "resume_" + resumeId + ".pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (NoSuchElementException e) {
            log.warn("下载简历失败: resumeId={}, {}", resumeId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "简历不存在：" + e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IOException e) {
            log.error("读取简历PDF失败: resumeId={}", resumeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "读取PDF失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            log.error("下载简历失败: resumeId={}", resumeId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "下载简历失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 