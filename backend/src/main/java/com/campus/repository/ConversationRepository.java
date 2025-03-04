package com.campus.repository;

import com.campus.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Page<Conversation> findByStudentIdOrderByUpdateTimeDesc(Long studentId, Pageable pageable);
    Page<Conversation> findByCompanyIdOrderByUpdateTimeDesc(Long companyId, Pageable pageable);
    Page<Conversation> findByStudentIdOrCompanyId(Long studentId, Long companyId, Pageable pageable);
} 