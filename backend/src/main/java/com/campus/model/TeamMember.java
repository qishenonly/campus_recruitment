package com.campus.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
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
    
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 关联的用户，如果有登录账号
    
    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
    
    @Column(name = "update_time")
    private LocalDateTime updateTime = LocalDateTime.now();
    
    public Long getCompanyId() {
        return company != null ? company.getId() : null;
    }
    
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    public void setUserId(Long userId) {
        // 此方法留空，使用 setUser 方法替代
    }
} 