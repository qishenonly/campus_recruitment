package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "team_members")
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long companyId; // 所属公司ID
    
    @Column(nullable = false)
    private String name; // 姓名
    
    @Column(nullable = false)
    private String position; // 职位
    
    @Column(nullable = false)
    private String email; // 邮箱
    
    private String phone; // 手机号
    
    @Column(nullable = false)
    private String role = "member"; // 角色，admin或member
    
    private String username; // 登录用户名
    
    private Long userId; // 关联的用户ID，如果有登录账号
    
    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
    
    @Column(name = "update_time")
    private LocalDateTime updateTime = LocalDateTime.now();
} 