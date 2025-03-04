package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "real_name")
    private String realName;
    
    @Column(name = "avatar")
    private String avatar;  // 头像URL
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    public enum UserRole {
        STUDENT, COMPANY, ADMIN
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, BLOCKED
    }

    public UserRole getRole() {
        return role;
    }
} 