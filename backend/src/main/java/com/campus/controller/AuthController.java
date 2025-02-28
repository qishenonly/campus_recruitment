package com.campus.controller;

import com.campus.service.EmailService;
import com.campus.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "邮箱地址不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            String code = generateVerificationCode();
            verificationCodeService.saveCode(email, code); // 保存验证码到Redis
            emailService.sendVerificationEmail(email, code);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "验证码发送成功");
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "发送邮件失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 验证验证码
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        if (email == null || code == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "邮箱和验证码不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean isValid = verificationCodeService.verifyCode(email, code);
        Map<String, Object> response = new HashMap<>();
        if (isValid) {
            response.put("code", 200);
            response.put("message", "验证成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 400);
            response.put("message", "验证码无效或已过期");
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 