package com.campus.controller;

import com.campus.model.User;
import com.campus.service.EmailService;
import com.campus.service.UserService;
import com.campus.service.VerificationCodeService;
import com.campus.service.UsernameGeneratorService;
import com.campus.service.RegistrationValidationService;
import com.campus.dto.ResponseDTO;
import com.campus.util.JwtUtil;
import com.campus.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
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
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UsernameGeneratorService usernameGenerator;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RegistrationValidationService registrationValidationService;

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String password = request.get("password");
        String role = request.get("role");
        
        // 参数验证
        if (email == null || code == null || password == null || role == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "所有字段都是必填的");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 验证是否允许注册
        ResponseDTO<Void> registerEnabledResult = registrationValidationService.validateRegistrationEnabled();
        if (registerEnabledResult != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", registerEnabledResult.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        
        // 根据角色验证是否允许该类型用户注册
        if ("student".equalsIgnoreCase(role)) {
            ResponseDTO<Void> studentRegisterResult = registrationValidationService.validateStudentRegistrationEnabled();
            if (studentRegisterResult != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", studentRegisterResult.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证学生邮箱
            ResponseDTO<Void> emailValidateResult = registrationValidationService.validateStudentEmail(email);
            if (emailValidateResult != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", emailValidateResult.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } else if ("company".equalsIgnoreCase(role)) {
            ResponseDTO<Void> companyRegisterResult = registrationValidationService.validateCompanyRegistrationEnabled();
            if (companyRegisterResult != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", companyRegisterResult.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        // 验证密码复杂度
        ResponseDTO<Void> passwordValidateResult = registrationValidationService.validatePassword(password);
        if (passwordValidateResult != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", passwordValidateResult.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        
        // 验证验证码
        if (!verificationCodeService.verifyCode(email, code)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "验证码无效或已过期");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 检查邮箱是否已注册
            if (userService.existsByEmail(email)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "该邮箱已注册");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 生成用户名
            String username = usernameGenerator.generateUsername();
            while (userService.existsByUsername(username)) {
                username = usernameGenerator.generateUsername();
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(PasswordEncoder.encode(password)); // 使用SHA-256加密密码
            user.setEmail(email);
            user.setRole(User.UserRole.valueOf(role.toUpperCase()));
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            // 如果是企业用户，需要根据系统设置确定状态
            if (user.getRole() == User.UserRole.COMPANY) {
                // 检查企业是否需要审核
                boolean companyNeedVerify = checkCompanyNeedVerify();
                user.setStatus(companyNeedVerify ? User.UserStatus.INACTIVE : User.UserStatus.ACTIVE);
            } else {
                user.setStatus(User.UserStatus.ACTIVE); // 默认激活状态
            }
            
            userService.save(user);
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "注册成功");
            response.put("data", Map.of(
                "username", username,
                "email", email,
                "role", role,
                "status", user.getStatus().toString()
            ));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 检查企业是否需要审核
     * @return 是否需要审核
     */
    private boolean checkCompanyNeedVerify() {
        // 通过系统设置查询企业是否需要审核
        return userService.getSystemSettingBooleanValue("register.companyNeedVerify", true);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        // 参数验证
        if (email == null || password == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "邮箱和密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 查找用户
            User user = userService.findByEmail(email);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证密码
            if (!PasswordEncoder.matches(password, user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "密码错误");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 生成token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "token", token
            ));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "登录失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");
        
        // 参数验证
        if (email == null || code == null || newPassword == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "所有字段都是必填的");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 验证验证码
        if (!verificationCodeService.verifyCode(email, code)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "验证码无效或已过期");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 更新密码
            User user = userService.findByEmail(email);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            user.setPassword(PasswordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            userService.save(user);
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "密码修改失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 