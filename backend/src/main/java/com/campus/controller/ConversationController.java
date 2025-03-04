package com.campus.controller;

import com.campus.model.Conversation;
import com.campus.model.Message;
import com.campus.model.User;
import com.campus.service.ConversationService;
import com.campus.service.UserService;
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

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getConversations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            Page<Conversation> conversations = conversationService.getUserConversations(
                userId, 
                PageRequest.of(page, size)
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", conversations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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