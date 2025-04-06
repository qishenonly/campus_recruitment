package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interviews")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String candidateName;
    
    private String candidatePhone;
    
    @Column(nullable = false)
    private String candidateEmail;  // 新增字段
    
    @Column(nullable = false)
    private Long jobId;
    
    private String jobTitle;
    
    @Column(name = "interview_time", nullable = false)
    private LocalDateTime interviewTime;
    
    @Column(nullable = false)
    private Integer duration = 60; // 默认面试时长60分钟
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type;
    
    private String location; // 线下面试地点
    
    private String onlineUrl; // 线上面试链接
    
    @Column(columnDefinition = "TEXT")
    private String description; // 面试说明
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String feedback; // 面试反馈
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    @Column(name = "application_id")
    private Long applicationId;  // 允许为空
    
    public enum InterviewType {
        ONLINE, ONSITE
    }
    
    public enum InterviewStatus {
        PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELED
    }
} 