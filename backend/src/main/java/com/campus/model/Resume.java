package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long studentId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String education;
    
    @Column(columnDefinition = "TEXT")
    private String experience;
    
    @Column(columnDefinition = "TEXT")
    private String skills;
    
    @Column(columnDefinition = "TEXT")
    private String projects;
    
    @Column(columnDefinition = "TEXT")
    private String awards;
    
    @Column(columnDefinition = "TEXT")
    private String selfEvaluation;
    
    private String attachmentUrl;
    
    @Column(columnDefinition = "TEXT")
    private String content;  // 存储PDF解析后的文本内容
    
    // 增加简历解析相关字段
    private String positionApplied; // 应聘职位
    
    @Enumerated(EnumType.STRING)
    private ResumeStatus status = ResumeStatus.待处理; // 简历状态
    
    private String phone; // 解析出的电话
    
    private String email; // 解析出的邮箱
    
    private String school; // 解析出的学校
    
    private String major; // 解析出的专业
    
    private String graduateYear; // 解析出的毕业年份
    
    private LocalDateTime submitTime; // 提交时间
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    public enum ResumeStatus {
        待处理, 已查看, 面试中, 已录用, 已拒绝
    }
} 