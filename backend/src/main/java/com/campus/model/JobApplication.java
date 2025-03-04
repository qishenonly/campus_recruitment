package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long studentId;
    
    @Column(nullable = false)
    private Long jobId;
    
    @Column(nullable = false)
    private Long resumeId;
    
    @Column(columnDefinition = "TEXT")
    private String coverLetter;  // 求职信/自我介绍
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    public enum ApplicationStatus {
        PENDING,    // 待处理
        REVIEWING,  // 审核中
        ACCEPTED,   // 已接受
        REJECTED,   // 已拒绝
        WITHDRAWN   // 已撤回
    }
} 