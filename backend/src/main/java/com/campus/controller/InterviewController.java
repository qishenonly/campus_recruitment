package com.campus.controller;

import com.campus.model.Interview;
import com.campus.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PutMapping("/{interviewId}/status")
    public ResponseEntity<?> updateInterviewStatus(
            @PathVariable Long interviewId,
            @RequestParam Interview.InterviewStatus status,
            @RequestParam(required = false) String token) {
        try {
            // 更新面试状态
            interviewService.updateInterviewStatus(interviewId, status, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "面试状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "面试状态更新失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 