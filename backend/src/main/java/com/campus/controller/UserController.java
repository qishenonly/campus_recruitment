package com.campus.controller;

import com.campus.model.Student;
import com.campus.model.User;
import com.campus.model.TeamMember;
import com.campus.model.Company;
import com.campus.service.StudentService;
import com.campus.service.UserService;
import com.campus.service.CompanyService;
import com.campus.repository.TeamMemberRepository;
import com.campus.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private CompanyService companyService;

    @GetMapping("/user/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取学生信息
            Student student = studentService.findByUserId(userId)
                    .orElseGet(() -> {
                        // 如果不存在，返回一个空的Student对象
                        Student newStudent = new Student();
                        newStudent.setId(userId);
                        return newStudent;
                    });

            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", student.getId());
            userInfo.put("realName", student.getRealName());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("university", student.getUniversity());
            userInfo.put("major", student.getMajor());
            userInfo.put("education", student.getEducation());
            userInfo.put("graduationYear", student.getGraduationYear());
            userInfo.put("gender", student.getGender());
            userInfo.put("birth", student.getBirth());
            userInfo.put("location", student.getLocation());
            userInfo.put("expectedPosition", student.getExpectedPosition());
            userInfo.put("expectedSalary", student.getExpectedSalary());
            userInfo.put("expectedCity", student.getExpectedCity());
            userInfo.put("identity", user.getRole());
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "获取个人资料失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        try {
            // 先记录请求信息
            System.out.println("正在查询用户信息, userId=" + userId);
            
            // 尝试获取用户信息
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                System.out.println("用户不存在, userId=" + userId);
                Map<String, Object> error = new HashMap<>();
                error.put("code", 404);
                error.put("message", "用户不存在: userId=" + userId);
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            System.out.println("找到用户: " + user.getUsername() + ", 角色: " + user.getRole());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            
            // 根据用户角色获取更多信息
            if (user.getRole() == User.UserRole.COMPANY) {
                System.out.println("企业用户, 尝试获取团队成员信息");
                // 查找团队成员信息
                Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByUserId(userId);
                if (teamMemberOpt.isPresent()) {
                    TeamMember teamMember = teamMemberOpt.get();
                    System.out.println("找到团队成员: " + teamMember.getName() + ", 公司ID: " + teamMember.getCompanyId());
                    userInfo.put("position", teamMember.getPosition());
                    userInfo.put("name", teamMember.getName());
                    userInfo.put("email", teamMember.getEmail());
                    userInfo.put("phone", teamMember.getPhone());
                    userInfo.put("role", teamMember.getRole());
                    
                    // 获取公司信息
                    Long companyId = teamMember.getCompanyId();
                    if (companyId != null) {
                        System.out.println("尝试获取公司信息, companyId=" + companyId);
                        userInfo.put("companyId", companyId);
                        userInfo.put("companyName", companyService.findCompanyNameById(companyId));
                    }
                } else {
                    System.out.println("未找到团队成员信息, userId=" + userId);
                }
            } else if (user.getRole() == User.UserRole.STUDENT) {
                System.out.println("学生用户, 尝试获取学生信息");
                // 如果是学生用户，可以添加学生的额外信息
                Optional<Student> studentOpt = studentService.findByUserId(userId);
                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    System.out.println("找到学生信息: " + student.getRealName());
                    userInfo.put("realName", student.getRealName());
                    userInfo.put("university", student.getUniversity());
                    userInfo.put("major", student.getMajor());
                    userInfo.put("education", student.getEducation());
                } else {
                    System.out.println("未找到学生信息, userId=" + userId);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 记录详细的错误信息
            System.err.println("获取用户信息时发生错误:");
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "获取用户信息失败：" + e.getMessage());
            error.put("errorDetail", e.toString());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取企业用户信息 - 通过TeamMember关联查询
     * 这个方法将替代旧的企业用户查询逻辑
     */
    @GetMapping("/company-users/{userId}")
    public ResponseEntity<?> getCompanyUserInfo(@PathVariable Long userId) {
        try {
            System.out.println("正在查询企业用户信息, userId=" + userId);
            
            // 1. 先检查用户是否存在
            User user = userService.findById(userId)
                    .orElse(null);
                    
            if (user == null) {
                System.out.println("用户不存在, userId=" + userId);
                Map<String, Object> error = new HashMap<>();
                error.put("code", 404);
                error.put("message", "用户不存在");
                return ResponseEntity.status(404).body(error);
            }
            
            System.out.println("找到用户: " + user.getUsername() + ", 角色: " + user.getRole());
            
            // 2. 直接查询团队成员表
            Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByUserId(userId);
            if (!teamMemberOpt.isPresent()) {
                System.out.println("未找到关联的团队成员记录, userId=" + userId);
                Map<String, Object> error = new HashMap<>();
                error.put("code", 404);
                error.put("message", "未找到企业成员信息");
                return ResponseEntity.status(404).body(error);
            }
            
            TeamMember teamMember = teamMemberOpt.get();
            System.out.println("找到团队成员: " + teamMember.getName() + ", 公司ID: " + teamMember.getCompanyId());
            
            // 3. 构建返回数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            userInfo.put("memberName", teamMember.getName());
            userInfo.put("position", teamMember.getPosition());
            userInfo.put("memberEmail", teamMember.getEmail());
            userInfo.put("phone", teamMember.getPhone());
            userInfo.put("memberRole", teamMember.getRole());
            
            // 4. 获取公司信息
            Long companyId = teamMember.getCompanyId();
            if (companyId != null) {
                System.out.println("尝试获取公司信息, companyId=" + companyId);
                userInfo.put("companyId", companyId);
                userInfo.put("companyName", companyService.findCompanyNameById(companyId));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取企业用户信息时发生错误:");
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "获取企业用户信息失败：" + e.getMessage());
            error.put("errorDetail", e.toString());
            return ResponseEntity.internalServerError().body(error);
        }
    }
} 