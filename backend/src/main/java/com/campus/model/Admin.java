package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "real_name")
    private String realName;
    
    private String email;
    
    private String phone;
    
    @Column(name = "two_factor_auth", nullable = false)
    private Boolean twoFactorAuth = false;
    
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminStatus status = AdminStatus.ACTIVE;
    
    public enum AdminStatus {
        ACTIVE, LOCKED
    }
} 