package com.campus.controller;

import com.campus.model.JobApplication;
import com.campus.service.JobApplicationService;
import com.campus.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobApplicationController {

    @Autowired
    private JobApplicationService jobApplicationService;

    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> apply(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long studentId = JwtUtil.getUserIdFromToken(jwtToken);
            
            Long resumeId = Long.valueOf(request.get("resumeId").toString());
            String coverLetter = (String) request.get("coverLetter");
            
            Map<String, Object> result = jobApplicationService.apply(studentId, jobId, resumeId, coverLetter);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "投递成功");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "投递失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<?> getApplications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long studentId = JwtUtil.getUserIdFromToken(jwtToken);
            
            Page<JobApplication> applications = jobApplicationService.getStudentApplications(
                studentId, 
                PageRequest.of(page, size)
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", applications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取投递记录失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 