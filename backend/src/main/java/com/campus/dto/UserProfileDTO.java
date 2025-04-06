package com.campus.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String email;
    private String phone;
} 