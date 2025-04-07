package com.campus.dto;

import lombok.Data;

/**
 * 管理员DTO
 */
@Data
public class AdminDTO {
    /**
     * 管理员ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 是否启用两步验证
     */
    private Boolean twoFactorAuth;
} 