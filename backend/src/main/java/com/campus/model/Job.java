package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long companyId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    @Column(nullable = false)
    private String salary;
    
    @Column(nullable = false)
    private String location;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PositionType positionType;
    
    private String educationRequirement;
    
    private String majorRequirement;
    
    @Column(nullable = false)
    private LocalDateTime publishDate;
    
    @Column(nullable = false)
    private LocalDateTime deadline;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PUBLISHED;
    
    private Integer viewCount = 0;
    
    private Integer applyCount = 0;
    
    public enum PositionType {
        全职, 实习, 兼职
    }
    
    public enum JobStatus {
        DRAFT, PUBLISHED, CLOSED, DELETED
    }
} 