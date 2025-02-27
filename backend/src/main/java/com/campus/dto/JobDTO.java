package com.campus.dto;

import com.campus.model.Job;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobDTO {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String salary;
    private String location;
    private Job.PositionType positionType;
    private String educationRequirement;
    private String majorRequirement;
    private LocalDateTime publishDate;
    private LocalDateTime deadline;
    private Job.JobStatus status;
    private Integer viewCount;
    private Integer applyCount;
    
    // 公司信息
    private Long companyId;
    private String companyName;
    private String industry;
    private String companyLogo;
    private String companyScale;
    private Boolean companyVerified;
} 