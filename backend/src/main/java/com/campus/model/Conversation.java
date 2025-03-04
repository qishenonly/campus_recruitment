package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "application_id")
    private Long applicationId;  // 关联的投递记录ID
    
    @Column(name = "student_id")
    private Long studentId;
    
    @Column(name = "company_id")  // 这里实际上是HR的user_id
    private Long companyId;       // 存储发布职位的HR的user_id
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationStatus status = ConversationStatus.ACTIVE;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    public enum ConversationStatus {
        ACTIVE,   // 进行中
        CLOSED    // 已结束
    }
} 