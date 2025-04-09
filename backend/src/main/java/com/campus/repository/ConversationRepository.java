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
    Page<Conversation> findByCompanyId(Long companyId, Pageable pageable);
    
    // 新增通过公司实体ID查询对话的方法
    Page<Conversation> findByCompanyEntityIdOrderByUpdateTimeDesc(Long companyEntityId, Pageable pageable);
    
    // 新增通过公司员工ID查询对话的方法
    Page<Conversation> findByCompanyEmployeeIdOrderByUpdateTimeDesc(Long companyEmployeeId, Pageable pageable);
    
    // 新增通过公司实体ID或员工ID查询对话的方法
    Page<Conversation> findByCompanyEntityIdOrCompanyEmployeeId(Long companyEntityId, Long companyEmployeeId, Pageable pageable);
} 