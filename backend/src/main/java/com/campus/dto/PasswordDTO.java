package com.campus.dto;

import lombok.Data;

/**
 * 密码DTO
 */
@Data
public class PasswordDTO {
    /**
     * 当前密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
} 