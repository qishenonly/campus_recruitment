package com.campus.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;           // 职位标题
    private String company;         // 公司名称
    private String location;        // 工作地点
    private String description;     // 职位描述
    private String requirements;    // 职位要求
    private BigDecimal salary;      // 薪资
    private String education;       // 学历要求
    private LocalDateTime deadline; // 投递截止日期
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
} 