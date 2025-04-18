package com.campus.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    // 一个用户可以是多个团队的成员
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers = new ArrayList<>();
    
    public enum UserRole {
        STUDENT, COMPANY, ADMIN
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, BLOCKED
    }

    public UserRole getRole() {
        return role;
    }
    
    // 添加和移除团队成员的便捷方法
    public void addTeamMember(TeamMember member) {
        teamMembers.add(member);
        member.setUser(this);
    }
    
    public void removeTeamMember(TeamMember member) {
        teamMembers.remove(member);
        member.setUser(null);
    }
} 