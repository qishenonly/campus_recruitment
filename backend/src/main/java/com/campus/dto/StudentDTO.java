package com.campus.dto;

import lombok.Data;

/**
 * 学生DTO
 */
@Data
public class StudentDTO {
    /**
     * 学生ID
     */
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 学校
     */
    private String school;

    /**
     * 专业
     */
    private String major;

    /**
     * 学历
     */
    private String education;

    /**
     * 毕业年份
     */
    private String graduationYear;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 注册时间
     */
    private String registerTime;

    /**
     * 账号状态
     */
    private String status;
} 