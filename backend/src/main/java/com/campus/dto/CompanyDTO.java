package com.campus.dto;

import lombok.Data;

/**
 * 企业DTO
 */
@Data
public class CompanyDTO {
    /**
     * 企业ID
     */
    private Long id;

    /**
     * 企业名称
     */
    private String name;

    /**
     * 行业
     */
    private String industry;

    /**
     * 规模
     */
    private String scale;

    /**
     * 所在地
     */
    private String location;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 职位数
     */
    private Integer jobCount;

    /**
     * 注册时间
     */
    private String registerTime;

    /**
     * 账号状态
     */
    private String status;
} 