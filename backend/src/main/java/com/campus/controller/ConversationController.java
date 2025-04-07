package com.campus.controller;

import com.campus.model.Conversation;
import com.campus.model.Message;
import com.campus.model.User;
import com.campus.model.Job;
import com.campus.model.JobApplication;
import com.campus.service.ConversationService;
import com.campus.service.UserService;
import com.campus.service.JobService;
import com.campus.service.JobApplicationService;
import com.campus.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private JobApplicationService jobApplicationService;

    @GetMapping
    public ResponseEntity<?> getConversations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            String role = JwtUtil.getRoleFromToken(jwtToken);
            
            Page<Conversation> conversationsPage;
            
            if ("STUDENT".equals(role)) {
                conversationsPage = conversationService.findByStudentId(userId, PageRequest.of(page, size));
            } else if ("COMPANY".equals(role)) {
                conversationsPage = conversationService.findByCompanyId(userId, PageRequest.of(page, size));
            } else {
                throw new RuntimeException("无效的用户角色");
            }
            
            // 转换分页数据并添加额外信息
            List<Map<String, Object>> enhancedConversations = new ArrayList<>();
            
            for (Conversation conversation : conversationsPage.getContent()) {
                Map<String, Object> conversationData = new HashMap<>();
                // 复制基本属性
                conversationData.put("id", conversation.getId());
                conversationData.put("applicationId", conversation.getApplicationId());
                conversationData.put("studentId", conversation.getStudentId());
                conversationData.put("companyId", conversation.getCompanyId());
                conversationData.put("status", conversation.getStatus());
                conversationData.put("createTime", conversation.getCreateTime());
                conversationData.put("updateTime", conversation.getUpdateTime());
                
                // 获取职位信息和发布者信息
                try {
                    // 通过applicationId获取JobApplication
                    JobApplication application = jobApplicationService.findById(conversation.getApplicationId());
                    if (application != null) {
                        Long jobId = application.getJobId();
                        
                        // 添加职位信息
                        jobService.findByIdWithCompany(jobId).ifPresent(jobDTO -> {
                            conversationData.put("jobId", jobId);
                            conversationData.put("jobTitle", jobDTO.getTitle());
                            conversationData.put("publisherId", jobDTO.getPublisherId());
                            conversationData.put("publisherName", jobDTO.getPublisherName());
                            conversationData.put("publisherPosition", jobDTO.getPublisherPosition());
                            conversationData.put("companyName", jobDTO.getCompanyName());
                        });
                    }
                } catch (Exception e) {
                    System.err.println("获取职位信息失败: " + e.getMessage());
                    // 继续执行，不要因为获取额外信息失败而中断整个请求
                }
                
                enhancedConversations.add(conversationData);
            }
            
            // 创建分页结果
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("content", enhancedConversations);
            pageResult.put("pageable", conversationsPage.getPageable());
            pageResult.put("totalElements", conversationsPage.getTotalElements());
            pageResult.put("totalPages", conversationsPage.getTotalPages());
            pageResult.put("last", conversationsPage.isLast());
            pageResult.put("numberOfElements", conversationsPage.getNumberOfElements());
            pageResult.put("first", conversationsPage.isFirst());
            pageResult.put("size", conversationsPage.getSize());
            pageResult.put("number", conversationsPage.getNumber());
            pageResult.put("sort", conversationsPage.getSort());
            pageResult.put("empty", conversationsPage.isEmpty());
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", pageResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取对话列表失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            Page<Message> messages = conversationService.getMessages(
                conversationId,
                userId,
                PageRequest.of(page, size)
            );
            
            List<Map<String, Object>> messagesWithRole = new ArrayList<>();
            
            for (Message message : messages) {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", message.getId());
                messageData.put("content", message.getContent());
                messageData.put("senderId", message.getSenderId());
                messageData.put("createTime", message.getCreateTime());
                messageData.put("isRead", message.getIsRead());
                
                User sender = userService.findById(message.getSenderId())
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                messageData.put("senderRole", sender.getRole().name());
                messageData.put("senderAvatar", sender.getAvatar());
                
                messagesWithRole.add(messageData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", messagesWithRole);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取消息失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            String content = request.get("content");
            Message message = conversationService.sendMessage(conversationId, userId, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "发送成功");
            response.put("data", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "发送消息失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 