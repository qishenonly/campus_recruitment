package com.campus.controller;

import com.campus.model.JobFavorite;
import com.campus.service.JobFavoriteService;
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
public class JobFavoriteController {

    @Autowired
    private JobFavoriteService jobFavoriteService;

    @PostMapping("/{jobId}/favorite")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long studentId = JwtUtil.getUserIdFromToken(jwtToken);
            
            jobFavoriteService.addFavorite(studentId, jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "收藏成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "收藏失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{jobId}/favorite")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long studentId = JwtUtil.getUserIdFromToken(jwtToken);
            
            jobFavoriteService.removeFavorite(studentId, jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "取消收藏成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "取消收藏失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long studentId = JwtUtil.getUserIdFromToken(jwtToken);
            
            Page<JobFavorite> favorites = jobFavoriteService.getFavorites(
                studentId, 
                PageRequest.of(page, size)
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", favorites);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取收藏列表失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{jobId}/favorite")
    public ResponseEntity<?> isFavorited(
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long studentId = JwtUtil.getUserIdFromToken(jwtToken);
            
            boolean favorited = jobFavoriteService.isFavorited(studentId, jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", favorited);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询收藏状态失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 