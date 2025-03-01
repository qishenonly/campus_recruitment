package com.campus.controller;

import com.campus.model.Student;
import com.campus.service.StudentService;
import com.campus.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            // 从token中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取学生信息
            Optional<Student> studentOpt = studentService.findByUserId(userId);
            
            if (studentOpt.isEmpty()) {
                // 如果还没有创建资料，返回空数据
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "success");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", studentOpt.get());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取资料失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        try {
            // 从token中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取或创建学生信息
            Student student = studentService.findByUserId(userId)
                    .orElseGet(Student::new);
            
            // 设置基本信息
            student.setRealName(request.get("name"));
            student.setUniversity(request.get("school"));
            student.setMajor(request.get("major"));
            student.setEducation(Student.Education.valueOf(request.get("education")));
            student.setGraduationYear(request.get("graduationYear"));
            student.setGender(Student.Gender.valueOf(request.get("gender")));
            student.setBirth(LocalDate.parse(request.get("birthday")));
            student.setLocation(request.get("location"));
            
            // 设置期望信息
            student.setExpectedPosition(request.get("expectedPosition"));
            student.setExpectedSalary(request.get("expectedSalary"));
            student.setExpectedCity(request.get("expectedLocation"));
            
            Student savedStudent = studentService.save(userId, student);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "个人资料更新成功");
            response.put("data", savedStudent);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 