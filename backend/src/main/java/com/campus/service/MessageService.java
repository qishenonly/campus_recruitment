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
} 