package com.campus.repository;

import com.campus.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 分页查询会话消息
    Page<Message> findByConversationIdOrderByCreateTimeDesc(Long conversationId, Pageable pageable);
    
    // 标记消息为已读
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.id = :messageId AND m.conversationId = :conversationId")
    void markAsRead(Long messageId, Long conversationId);

    // 根据会话ID将所有消息标记为已读（新方法）
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.senderId != :userId AND m.isRead = false")
    void markMessagesAsReadInConversation(Long conversationId, Long userId);

    Message findFirstByConversationIdOrderByCreateTimeDesc(Long conversationId);

    long countByConversationIdAndSenderIdNotAndIsReadFalse(Long conversationId, Long userId);
}