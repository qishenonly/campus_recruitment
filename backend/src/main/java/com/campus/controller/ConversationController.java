package com.campus.controller;

import com.campus.model.Conversation;
import com.campus.model.Message;
import com.campus.model.TeamMember;
import com.campus.model.User;
import com.campus.model.Job;
import com.campus.model.JobApplication;
import com.campus.repository.ConversationRepository;
import com.campus.repository.TeamMemberRepository;
import com.campus.service.ConversationService;
import com.campus.service.UserService;
import com.campus.service.JobService;
import com.campus.service.JobApplicationService;
import com.campus.service.MessageService;
import com.campus.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

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

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;

    @GetMapping
    public ResponseEntity<?> getConversations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Page<Conversation> conversations;
            Pageable pageable = PageRequest.of(page, size);
            
            // 根据用户类型返回不同的会话列表
            if (user.getRole() == User.UserRole.STUDENT) {
                // 学生用户查看自己的会话
                conversations = conversationService.findByStudentId(userId, pageable);
            } else if (user.getRole() == User.UserRole.COMPANY) {
                // 公司用户查看自己和所属公司的会话
                // 先查找用户是否是公司员工
                Optional<TeamMember> teamMember = teamMemberRepository.findByUserId(userId);
                
                if (teamMember.isPresent()) {
                    // 员工可以查看所属公司的会话或分配给自己的会话
                    Long companyEntityId = teamMember.get().getCompanyId();
                    Long employeeId = teamMember.get().getId();
                    
                    if ("assigned".equals(type)) {
                        // 只查看分配给自己的会话
                        conversations = conversationService.getEmployeeConversations(employeeId, pageable);
                    } else if ("company".equals(type)) {
                        // 查看公司的所有会话
                        conversations = conversationService.getCompanyConversationsByEntityId(companyEntityId, pageable);
                    } else {
                        // 查看公司的所有会话，包括分配给自己的会话
                        conversations = conversationService.getCompanyOrEmployeeConversations(companyEntityId, employeeId, pageable);
                    }
                } else {
                    // 如果不是公司员工，只能查看直接关联自己的会话
                    conversations = conversationService.findByCompanyId(userId, pageable);
                }
            } else {
                // 管理员可以查看所有会话（这里可以根据需要添加限制）
                conversations = conversationRepository.findAll(pageable);
            }
            
            // 转换分页数据并添加额外信息
            List<Map<String, Object>> enhancedConversations = new ArrayList<>();
            
            for (Conversation conversation : conversations.getContent()) {
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
            pageResult.put("pageable", conversations.getPageable());
            pageResult.put("totalElements", conversations.getTotalElements());
            pageResult.put("totalPages", conversations.getTotalPages());
            pageResult.put("last", conversations.isLast());
            pageResult.put("numberOfElements", conversations.getNumberOfElements());
            pageResult.put("first", conversations.isFirst());
            pageResult.put("size", conversations.getSize());
            pageResult.put("number", conversations.getNumber());
            pageResult.put("sort", conversations.getSort());
            pageResult.put("empty", conversations.isEmpty());
            
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

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<?> markConversationAsRead(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 验证用户是否有权限访问该对话
            Conversation conversation = conversationService.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("对话不存在"));
                    
            if (!conversation.getStudentId().equals(userId) && !conversation.getCompanyId().equals(userId)) {
                throw new RuntimeException("无权访问该对话");
            }
            
            // 标记所有消息为已读
            messageService.markConversationAsRead(conversationId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "所有消息已标记为已读");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "标记消息失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/{conversationId}/messages/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 验证用户是否有权限访问该对话
            Conversation conversation = conversationService.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("对话不存在"));
                    
            if (!conversation.getStudentId().equals(userId) && !conversation.getCompanyId().equals(userId)) {
                throw new RuntimeException("无权访问该对话");
            }
            
            // 标记单条消息为已读
            messageService.markAsRead(messageId, conversationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "消息已标记为已读");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "标记消息失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 