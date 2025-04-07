package com.campus.controller;

import com.campus.dto.PasswordChangeDTO;
import com.campus.dto.StudentSettingsDTO;
import com.campus.service.StudentSettingsService;
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
@RequestMapping("/api/student/settings")
public class StudentSettingsController {

    private final StudentSettingsService studentSettingsService;

    /**
     * 获取学生个人信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getStudentProfile(@RequestHeader("Authorization") String token) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取学生个人信息
            StudentSettingsDTO studentProfile = studentSettingsService.getStudentProfile(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取学生信息成功");
            response.put("data", studentProfile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取学生信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取学生信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 更新学生个人信息
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateStudentProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody StudentSettingsDTO profileDTO) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 更新学生个人信息
            StudentSettingsDTO updatedProfile = studentSettingsService.updateStudentProfile(userId, profileDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新学生信息成功");
            response.put("data", updatedProfile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新学生信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新学生信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 上传学生头像
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 上传头像
            String avatarUrl = studentSettingsService.uploadStudentAvatar(userId, file);
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", avatarUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "头像上传成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "头像上传失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody PasswordChangeDTO passwordDTO) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 修改密码
            boolean success = studentSettingsService.changePassword(userId, passwordDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "修改密码成功");
            response.put("data", success);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "修改密码失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/send-phone-code")
    public ResponseEntity<?> sendPhoneCode(
            @RequestParam("phone") String phone) {
        try {
            // 发送手机验证码
            boolean success = studentSettingsService.sendPhoneCode(phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "验证码发送成功");
            response.put("data", success);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "发送验证码失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 绑定手机号
     */
    @PostMapping("/bind-phone")
    public ResponseEntity<?> bindPhone(
            @RequestHeader("Authorization") String token,
            @RequestParam("phone") String phone,
            @RequestParam("code") String code) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 绑定手机号
            boolean success = studentSettingsService.bindPhone(userId, phone, code);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "手机号绑定成功");
            response.put("data", success);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("手机号绑定失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "手机号绑定失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-email-code")
    public ResponseEntity<?> sendEmailCode(
            @RequestParam("email") String email) {
        try {
            // 发送邮箱验证码
            boolean success = studentSettingsService.sendEmailCode(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "验证码发送成功");
            response.put("data", success);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "发送验证码失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 绑定邮箱
     */
    @PostMapping("/bind-email")
    public ResponseEntity<?> bindEmail(
            @RequestHeader("Authorization") String token,
            @RequestParam("email") String email,
            @RequestParam("code") String code) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 绑定邮箱
            boolean success = studentSettingsService.bindEmail(userId, email, code);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "邮箱绑定成功");
            response.put("data", success);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("邮箱绑定失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "邮箱绑定失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 