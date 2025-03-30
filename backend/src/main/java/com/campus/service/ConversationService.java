package com.campus.service;

import com.campus.model.Conversation;
import com.campus.model.Message;
import com.campus.repository.ConversationRepository;
import com.campus.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConversationService {
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    public Page<Conversation> getUserConversations(Long userId, Pageable pageable) {
        return conversationRepository.findByStudentIdOrCompanyId(userId, userId, pageable);
    }
    
    public Page<Message> getMessages(Long conversationId, Long userId, Pageable pageable) {
        // 验证用户是否有权限访问该对话
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话不存在"));
                
        if (!conversation.getStudentId().equals(userId) && !conversation.getCompanyId().equals(userId)) {
            throw new RuntimeException("无权访问该对话");
        }
        
        // 将对方发送的未读消息标记为已读
        messageRepository.markAsRead(conversationId, userId);
        
        return messageRepository.findByConversationIdOrderByCreateTimeDesc(conversationId, pageable);
    }
    
    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, String content) {
        // 验证用户是否有权限访问该对话
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话不存在"));
                
        if (!conversation.getStudentId().equals(senderId) && !conversation.getCompanyId().equals(senderId)) {
            throw new RuntimeException("无权访问该对话");
        }
        
        // 创建新消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        
        // 更新对话的更新时间
        conversation.setUpdateTime(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return messageRepository.save(message);
    }

    public Conversation save(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Page<Conversation> findByStudentId(Long studentId, Pageable pageable) {
        return conversationRepository.findByStudentIdOrderByUpdateTimeDesc(studentId, pageable);
    }

    public Page<Conversation> findByCompanyId(Long companyId, Pageable pageable) {
        return conversationRepository.findByCompanyIdOrderByUpdateTimeDesc(companyId, pageable);
    }

    @Transactional
    public void updateStatus(Long id, Conversation.ConversationStatus status) {
        conversationRepository.findById(id).ifPresent(conversation -> {
            conversation.setStatus(status);
            conversation.setUpdateTime(LocalDateTime.now());
            conversationRepository.save(conversation);
        });
    }

    public Page<Conversation> getCompanyConversations(Long companyId, Pageable pageable) {
        return conversationRepository.findByCompanyId(companyId, pageable);
    }

    public Message getLastMessage(Long conversationId) {
        return messageRepository.findFirstByConversationIdOrderByCreateTimeDesc(conversationId);
    }

    public long getUnreadMessageCount(Long conversationId, Long userId) {
        return messageRepository.countByConversationIdAndSenderIdNotAndIsReadFalse(conversationId, userId);
    }
} 