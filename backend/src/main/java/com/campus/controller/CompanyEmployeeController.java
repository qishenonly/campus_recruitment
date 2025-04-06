package com.campus.controller;

import com.campus.model.Job;
import com.campus.model.User;
import com.campus.service.CompanyService;
import com.campus.service.JobService;
import com.campus.service.UserService;
import com.campus.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/company/employee")
public class CompanyEmployeeController {

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private CompanyService companyService;

    // 发布新职位
    @PostMapping("/jobs")
    public ResponseEntity<?> publishJob(
            @RequestHeader("Authorization") String token,
            @RequestBody Job job) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            String role = JwtUtil.getRoleFromToken(jwtToken);
            
            // 验证用户是否为企业用户
            if (!role.equals("COMPANY")) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "权限不足，只有企业用户可以发布职位");
                return ResponseEntity.status(403).body(response);
            }
            
            // 获取用户信息
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 获取企业信息
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 设置职位信息
            job.setCompanyId(companyId);
            job.setPublisherId(userId);
            job.setPublisherName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            job.setPublisherPosition("HR"); // 可以从请求中获取或设置默认值
            job.setPublishDate(LocalDateTime.now());
            job.setStatus(Job.JobStatus.PUBLISHED);
            job.setViewCount(0);
            job.setApplyCount(0);
            job.setDeadline(LocalDateTime.now().plusDays(30));
            
            // 保存职位
            Job savedJob = jobService.save(job);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "职位发布成功");
            response.put("data", savedJob);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "职位发布失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 获取企业发布的所有职位
    @GetMapping("/jobs")
    public ResponseEntity<?> getCompanyJobs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取职位列表
            Page<Job> jobs = jobService.findByCompanyId(companyId, PageRequest.of(page, size));
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", jobs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取职位列表失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 更新职位信息
    @PutMapping("/jobs/{jobId}")
    public ResponseEntity<?> updateJob(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId,
            @RequestBody Job job) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 验证职位是否属于该企业
            Job existingJob = jobService.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("职位不存在"));
            
            if (!existingJob.getCompanyId().equals(companyId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "权限不足，只能修改本企业发布的职位");
                return ResponseEntity.status(403).body(response);
            }
            
            // 更新职位信息，保留原有的不可修改字段
            job.setId(jobId);
            job.setCompanyId(companyId);
            job.setPublisherId(existingJob.getPublisherId());
            job.setPublisherName(existingJob.getPublisherName());
            job.setPublisherPosition(existingJob.getPublisherPosition());
            job.setPublishDate(existingJob.getPublishDate());
            job.setViewCount(existingJob.getViewCount());
            job.setApplyCount(existingJob.getApplyCount());
            job.setDeadline(existingJob.getDeadline());
            
            // 保存更新
            Job updatedJob = jobService.save(job);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "职位更新成功");
            response.put("data", updatedJob);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "职位更新失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 更改职位状态（关闭、删除等）
    @PutMapping("/jobs/{jobId}/status")
    public ResponseEntity<?> updateJobStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long jobId,
            @RequestParam Job.JobStatus status) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 验证职位是否属于该企业
            Job existingJob = jobService.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("职位不存在"));
            
            if (!existingJob.getCompanyId().equals(companyId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "权限不足，只能修改本企业发布的职位");
                return ResponseEntity.status(403).body(response);
            }
            
            // 更新状态
            jobService.updateStatus(jobId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "职位状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "职位状态更新失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // 获取收到的职位申请列表
    @GetMapping("/applications")
    public ResponseEntity<?> getJobApplications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取该企业所有职位收到的申请
            Page<?> applications = jobService.findApplicationsByCompanyId(companyId, PageRequest.of(page, size));
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", applications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取申请列表失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // 处理职位申请（接受、拒绝等）
    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long applicationId,
            @RequestParam String status) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 更新申请状态
            jobService.updateApplicationStatus(applicationId, companyId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "申请状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "申请状态更新失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 