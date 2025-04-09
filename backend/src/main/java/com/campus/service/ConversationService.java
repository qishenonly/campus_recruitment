package com.campus.service;

import com.campus.model.Conversation;
import com.campus.model.Message;
import com.campus.model.TeamMember;
import com.campus.repository.ConversationRepository;
import com.campus.repository.MessageRepository;
import com.campus.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConversationService {
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    // 获取用户的所有会话（学生或公司用户）
    public Page<Conversation> getUserConversations(Long userId, Pageable pageable) {
        return conversationRepository.findByStudentIdOrCompanyId(userId, userId, pageable);
    }
    
    // 获取公司所有会话（基于公司实体ID）
    public Page<Conversation> getCompanyConversationsByEntityId(Long companyEntityId, Pageable pageable) {
        return conversationRepository.findByCompanyEntityIdOrderByUpdateTimeDesc(companyEntityId, pageable);
    }
    
    // 获取员工处理的所有会话（基于员工ID）
    public Page<Conversation> getEmployeeConversations(Long employeeId, Pageable pageable) {
        return conversationRepository.findByCompanyEmployeeIdOrderByUpdateTimeDesc(employeeId, pageable);
    }
    
    // 获取公司或员工的所有会话
    public Page<Conversation> getCompanyOrEmployeeConversations(Long companyEntityId, Long employeeId, Pageable pageable) {
        return conversationRepository.findByCompanyEntityIdOrCompanyEmployeeId(companyEntityId, employeeId, pageable);
    }
    
    // 获取会话中的消息
    public Page<Message> getMessages(Long conversationId, Long userId, Pageable pageable) {
        // 验证用户是否有权限访问该对话
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话不存在"));
                
        if (!conversation.getStudentId().equals(userId) && !conversation.getCompanyId().equals(userId) && 
            !isCompanyEmployee(userId, conversation.getCompanyEntityId())) {
            throw new RuntimeException("无权访问该对话");
        }
        
        // 将对方发送的未读消息标记为已读
        messageRepository.markMessagesAsReadInConversation(conversationId, userId);
        
        return messageRepository.findByConversationIdOrderByCreateTimeDesc(conversationId, pageable);
    }
    
    // 判断用户是否是公司员工
    private boolean isCompanyEmployee(Long userId, Long companyEntityId) {
        if (userId == null || companyEntityId == null) {
            return false;
        }
        return teamMemberRepository.findByUserIdAndCompanyId(userId, companyEntityId).isPresent();
    }
    
    // 发送消息
    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, String content) {
        // 验证用户是否有权限访问该对话
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话不存在"));
                
        if (!conversation.getStudentId().equals(senderId) && !conversation.getCompanyId().equals(senderId) && 
            !isCompanyEmployee(senderId, conversation.getCompanyEntityId())) {
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

    // 创建或更新会话
    @Transactional
    public Conversation createOrUpdateConversation(Long applicationId, Long studentId, Long companyUserId, Long companyEntityId, Long employeeId) {
        Optional<Conversation> existingConversation = conversationRepository.findById(applicationId);
        
        if (existingConversation.isPresent()) {
            Conversation conversation = existingConversation.get();
            conversation.setUpdateTime(LocalDateTime.now());
            // 如果提供了新的员工ID，则更新
            if (employeeId != null) {
                conversation.setCompanyEmployeeId(employeeId);
            }
            return conversationRepository.save(conversation);
        } else {
            Conversation conversation = new Conversation();
            conversation.setApplicationId(applicationId);
            conversation.setStudentId(studentId);
            conversation.setCompanyId(companyUserId);
            conversation.setCompanyEntityId(companyEntityId);
            conversation.setCompanyEmployeeId(employeeId);
            conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            return conversationRepository.save(conversation);
        }
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
    
    /**
     * 根据ID查找会话
     * @param id 会话ID
     * @return 会话Optional
     */
    public Optional<Conversation> findById(Long id) {
        return conversationRepository.findById(id);
    }
} 