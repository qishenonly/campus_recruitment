package com.campus.controller;

import com.campus.model.Resume;
import com.campus.service.ResumeParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/resume-parse")
@RequiredArgsConstructor
public class ResumeParseController {

    private final ResumeParseService resumeParseService;
    
    /**
     * 上传并解析简历
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndParseResume(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "resumeName", required = false) String resumeName) {
        log.info("收到简历上传请求: file={}, studentId={}, resumeName={}", 
                file != null ? file.getOriginalFilename() : "null", studentId, resumeName);
        
        try {
            // 参数校验
            Map<String, String> errors = new HashMap<>();
            
            if (file == null || file.isEmpty()) {
                log.warn("简历文件为空");
                errors.put("file", "简历文件不能为空");
            } else if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                log.warn("不支持的文件格式: {}", file.getOriginalFilename());
                errors.put("file", "仅支持PDF格式的简历文件");
            }
            
            if (studentId == null) {
                log.warn("学生ID为空");
                errors.put("studentId", "学生ID不能为空");
            }
            
            if (!errors.isEmpty()) {
                log.warn("简历上传参数错误: {}", errors);
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "请求参数错误");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 如果提供了简历名称，使用提供的名称；否则使用解析时的默认处理
            if (resumeName != null && !resumeName.trim().isEmpty()) {
                // 自定义处理简历名称
                log.info("使用客户端提供的简历名称: {}", resumeName);
            }
            
            // 处理上传和解析
            log.info("开始处理简历上传: studentId={}, filename={}, size={}bytes", 
                    studentId, file.getOriginalFilename(), file.getSize());
            
            Resume parsedResume = resumeParseService.uploadAndParseResume(file, studentId, resumeName);
            
            log.info("简历解析成功: resumeId={}, 名称={}", parsedResume.getId(), parsedResume.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "简历解析成功");
            response.put("data", parsedResume);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("简历上传参数错误", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            log.error("简历文件处理错误", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "文件处理错误: " + e.getMessage());
            response.put("error_details", e.toString());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            log.error("简历上传处理异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "服务器错误: " + e.getMessage());
            response.put("error_details", e.toString());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取简历PDF
     */
    @GetMapping("/pdf/{resumeId}")
    public ResponseEntity<?> getResumePdf(@PathVariable Long resumeId) {
        try {
            log.info("获取简历PDF: resumeId={}", resumeId);
            byte[] pdfBytes = resumeParseService.getResumePdf(resumeId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "resume.pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.warn("简历PDF不存在: resumeId={}", resumeId);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("读取简历PDF错误: resumeId={}", resumeId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "code", 500,
                "message", "文件读取错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新简历状态
     */
    @PutMapping("/{resumeId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long resumeId, 
            @RequestParam(required = false) String status) {
        try {
            // 参数校验
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "状态参数不能为空"
                ));
            }
            
            log.info("更新简历状态: resumeId={}, status={}", resumeId, status);
            Resume updatedResume = resumeParseService.updateStatus(resumeId, status);
            log.info("简历状态更新成功: resumeId={}, status={}", resumeId, updatedResume.getStatus());
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "更新状态成功",
                "data", updatedResume
            ));
        } catch (NoSuchElementException e) {
            log.warn("简历不存在: resumeId={}", resumeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "code", 404,
                "message", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            log.error("状态值无效: resumeId={}, status={}", resumeId, status);
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("更新简历状态错误: resumeId={}", resumeId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "code", 500,
                "message", "服务器错误: " + e.getMessage()
            ));
        }
    }
} 