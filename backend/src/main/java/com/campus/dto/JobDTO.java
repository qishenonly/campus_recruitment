package com.campus.dto;

import lombok.Data;

/**
 * 职位DTO
 */
@Data
public class JobDTO {
    /**
     * 职位ID
     */
    private Long id;

    /**
     * 职位名称
     */
    private String title;

    /**
     * 所属企业ID
     */
    private Long companyId;

    /**
     * 公司实体ID
     */
    private Long companyEntityId;

    /**
     * 所属企业名称
     */
    private String companyName;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 发布者姓名
     */
    private String publisherName;

    /**
     * 发布者职位
     */
    private String publisherPosition;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 企业Logo
     */
    private String companyLogo;

    /**
     * 企业规模
     */
    private String companyScale;

    /**
     * 企业是否认证
     */
    private Boolean companyVerified;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 薪资范围
     */
    private String salary;

    /**
     * 工作经验要求
     */
    private String experience;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 职位类别
     */
    private String category;

    /**
     * 职位描述
     */
    private String description;

    /**
     * 职位要求
     */
    private String requirement;

    /**
     * 发布时间
     */
    private String publishTime;

    /**
     * 截止时间
     */
    private String deadline;

    /**
     * 状态
     */
    private String status;

    /**
     * 投递人数
     */
    private Integer applyCount;
} 