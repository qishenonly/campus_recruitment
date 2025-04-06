package com.campus.controller;

import com.campus.model.Interview;
import com.campus.model.Job;
import com.campus.model.User;
import com.campus.service.CompanyService;
import com.campus.service.InterviewService;
import com.campus.service.JobService;
import com.campus.service.UserService;
import com.campus.service.EmailService;
import com.campus.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/company/interviews")
public class CompanyInterviewController {

    @Autowired
    private InterviewService interviewService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // 获取企业面试列表
    @GetMapping
    public ResponseEntity<?> getCompanyInterviews(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取面试列表
            Page<Interview> interviews = interviewService.findByCompanyId(
                companyId, 
                PageRequest.of(page, size)
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", interviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取面试列表失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 获取面试详情
    @GetMapping("/{interviewId}")
    public ResponseEntity<?> getInterviewDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long interviewId) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取面试详情
            Optional<Interview> interviewOpt = interviewService.findById(interviewId);
            if (!interviewOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "面试不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            Interview interview = interviewOpt.get();
            
            // 验证该面试是否属于该企业
            Optional<Job> jobOpt = jobService.findById(interview.getJobId());
            if (!jobOpt.isPresent() || !jobOpt.get().getCompanyId().equals(companyId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权访问此面试");
                return ResponseEntity.status(403).body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", interview);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取面试详情失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 检查候选人是否存在
    @GetMapping("/check-candidate")
    public ResponseEntity<?> checkCandidate(@RequestParam String name, @RequestParam(required = false) String phone) {
        try {
            // 根据姓名和电话号码检查候选人是否存在
            boolean exists = userService.existsByUsername(name);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "检查候选人失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 创建面试
    @PostMapping
    public ResponseEntity<?> createInterview(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 验证职位是否属于该企业
            Long jobId = Long.parseLong(request.get("jobId").toString());
            Optional<Job> jobOpt = jobService.findById(jobId);
            if (!jobOpt.isPresent() || !jobOpt.get().getCompanyId().equals(companyId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权为此职位创建面试");
                return ResponseEntity.status(403).body(response);
            }
            
            // 获取候选人邮箱
            Optional<User> userOpt = userService.findByUsername((String) request.get("candidateName"));
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "候选人不存在");
                return ResponseEntity.status(404).body(response);
            }
            User user = userOpt.get();
            String candidateEmail = user.getEmail();

            // 创建面试
            Interview interview = new Interview();
            interview.setCandidateName((String) request.get("candidateName"));
            interview.setCandidatePhone((String) request.get("candidatePhone"));
            interview.setJobId(jobId);
            interview.setJobTitle((String) request.get("jobTitle"));
            interview.setCandidateEmail(candidateEmail);
            interview.setCreateTime(LocalDateTime.now());
            interview.setUpdateTime(LocalDateTime.now());

            // 处理日期时间
            String interviewTimeStr = (String) request.get("interviewTime");
            LocalDateTime interviewTime;
            if (interviewTimeStr.contains("Z")) {
                // 处理ISO格式的日期时间
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(interviewTimeStr);
                interviewTime = zonedDateTime.toLocalDateTime();
            } else {
                // 处理其他格式的日期时间
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                interviewTime = LocalDateTime.parse(interviewTimeStr, formatter);
            }
            interview.setInterviewTime(interviewTime);
            
            // 设置面试类型
            String interviewTypeStr = (String) request.get("interviewType");
            Interview.InterviewType interviewType = Interview.InterviewType.valueOf(interviewTypeStr);
            interview.setType(interviewType);
            
            interview.setLocation((String) request.get("location"));
            interview.setDescription((String) request.get("description"));
            
            // 设置状态
            String statusStr = (String) request.get("status");
            if (statusStr != null) {
                interview.setStatus(Interview.InterviewStatus.valueOf(statusStr));
            }
            
            // 保存面试
            Interview savedInterview = interviewService.createInterview(interview);
            
            // 发送面试邀请邮件
            if (candidateEmail != null && !candidateEmail.isEmpty()) {
                String jobTitle = interview.getJobTitle();
                String companyName = companyService.findCompanyNameById(companyId);
                LocalDateTime interviewTimes = interview.getInterviewTime();
                String location = interview.getLocation();
                String interviewTypes = interview.getType().toString();
                
                emailService.sendInterviewInvitation(
                    candidateEmail, 
                    interview.getCandidateName(),
                    companyName,
                    jobTitle,
                    interviewTimes,
                    location,
                    interviewTypes,
                    interview.getDescription()
                );
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "面试创建成功");
            response.put("data", savedInterview);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建面试失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 更新面试信息
    @PutMapping("/{interviewId}")
    public ResponseEntity<?> updateInterview(
            @RequestHeader("Authorization") String token,
            @PathVariable Long interviewId,
            @RequestBody Map<String, Object> request) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取面试
            Optional<Interview> interviewOpt = interviewService.findById(interviewId);
            if (!interviewOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "面试不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            Interview interview = interviewOpt.get();
            
            // 验证该面试是否属于该企业
            Optional<Job> jobOpt = jobService.findById(interview.getJobId());
            if (!jobOpt.isPresent() || !jobOpt.get().getCompanyId().equals(companyId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权更新此面试");
                return ResponseEntity.status(403).body(response);
            }
            
            // 更新面试信息
            if (request.containsKey("candidateName")) {
                interview.setCandidateName((String) request.get("candidateName"));
            }
            if (request.containsKey("candidatePhone")) {
                interview.setCandidatePhone((String) request.get("candidatePhone"));
            }
            if (request.containsKey("jobTitle")) {
                interview.setJobTitle((String) request.get("jobTitle"));
            }
            if (request.containsKey("interviewTime")) {
                String interviewTimeStr = (String) request.get("interviewTime");
                LocalDateTime interviewTime;
                if (interviewTimeStr.contains("Z")) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(interviewTimeStr);
                    interviewTime = zonedDateTime.toLocalDateTime();
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                    interviewTime = LocalDateTime.parse(interviewTimeStr, formatter);
                }
                interview.setInterviewTime(interviewTime);
            }
            if (request.containsKey("interviewType")) {
                String interviewTypeStr = (String) request.get("interviewType");
                Interview.InterviewType interviewType = Interview.InterviewType.valueOf(interviewTypeStr);
                interview.setType(interviewType);
            }
            if (request.containsKey("location")) {
                interview.setLocation((String) request.get("location"));
            }
            if (request.containsKey("description")) {
                interview.setDescription((String) request.get("description"));
            }
            if (request.containsKey("feedback")) {
                interview.setFeedback((String) request.get("feedback"));
            }
            
            // 保存更新
            Interview updatedInterview = interviewService.updateInterview(interview);

            // 获取候选人邮箱
            Optional<User> userOpt = userService.findByUsername((String) request.get("candidateName"));
            if (!userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "候选人不存在");
                return ResponseEntity.status(404).body(response);
            }
            User user = userOpt.get();
            String candidateEmail = user.getEmail();

            // 发送面试邀请邮件
            if (candidateEmail != null && !candidateEmail.isEmpty()) {
                String jobTitle = interview.getJobTitle();
                String companyName = companyService.findCompanyNameById(companyId);
                LocalDateTime interviewTimes = interview.getInterviewTime();
                String location = interview.getLocation();
                String interviewTypes = interview.getType().toString();
                
                emailService.sendInterviewInvitation(
                    candidateEmail, 
                    interview.getCandidateName(),
                    companyName,
                    jobTitle,
                    interviewTimes,
                    location,
                    interviewTypes,
                    interview.getDescription()
                );
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "面试更新成功");
            response.put("data", updatedInterview);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新面试失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 更新面试状态
    @PutMapping("/{interviewId}/status")
    public ResponseEntity<?> updateInterviewStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long interviewId,
            @RequestBody Map<String, String> request) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取面试
            Optional<Interview> interviewOpt = interviewService.findById(interviewId);
            if (!interviewOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "面试不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            Interview interview = interviewOpt.get();
            
            // 验证该面试是否属于该企业
            Optional<Job> jobOpt = jobService.findById(interview.getJobId());
            if (!jobOpt.isPresent() || !jobOpt.get().getCompanyId().equals(companyId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权更新此面试状态");
                return ResponseEntity.status(403).body(response);
            }
            
            // 更新状态
            String status = request.get("status");
            Interview.InterviewStatus interviewStatus = Interview.InterviewStatus.valueOf(status);
            interviewService.updateStatus(interviewId, interviewStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "面试状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新面试状态失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 删除面试
    @DeleteMapping("/{interviewId}")
    public ResponseEntity<?> deleteInterview(
            @RequestHeader("Authorization") String token,
            @PathVariable Long interviewId) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取企业ID
            Long companyId = companyService.findCompanyIdByUserId(userId);
            
            // 获取面试
            Optional<Interview> interviewOpt = interviewService.findById(interviewId);
            if (!interviewOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "面试不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            Interview interview = interviewOpt.get();
            
            // 验证该面试是否属于该企业
            Optional<Job> jobOpt = jobService.findById(interview.getJobId());
            if (!jobOpt.isPresent() || !jobOpt.get().getCompanyId().equals(companyId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权删除此面试");
                return ResponseEntity.status(403).body(response);
            }
            
            // 删除面试
            interviewService.deleteInterview(interviewId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "面试删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除面试失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 