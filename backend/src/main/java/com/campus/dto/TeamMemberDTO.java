package com.campus.dto;

import lombok.Data;

@Data
public class TeamMemberDTO {
    private Long id;
    private String name; // 姓名
    private String position; // 职位
    private String email; // 邮箱
    private String phone; // 手机号
    private String role; // 角色
    private String password; // 密码(仅在创建时使用)
    private String confirmPassword; // 确认密码(仅在创建时使用)
} 