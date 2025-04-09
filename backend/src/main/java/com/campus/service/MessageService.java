package com.campus.service;

import com.campus.model.Message;
import com.campus.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public Page<Message> findByConversationId(Long conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderByCreateTimeDesc(conversationId, pageable);
    }

    @Transactional
    public void markAsRead(Long messageId, Long conversationId) {
        messageRepository.markAsRead(messageId, conversationId);
    }
    
    /**
     * 将会话中的所有消息标记为已读
     * @param conversationId 会话ID
     * @param userId 当前用户ID（排除此用户发送的消息）
     */
    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId) {
        messageRepository.markMessagesAsReadInConversation(conversationId, userId);
    }
} 