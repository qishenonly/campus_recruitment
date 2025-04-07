package com.campus.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String companyName;
    
    @Column(nullable = false)
    private String industry;
    
    private String scale;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String city; // 城市
    private String address; // 详细地址
    
    private String website;
    
    private String logo;
    
    private Boolean verified = false;
    
    private String verificationFiles;
    
    private String contactPerson;
    
    private String contactPosition;
    
    private String financingStage; // 融资阶段
    
    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
    
    @Column(name = "update_time")
    private LocalDateTime updateTime = LocalDateTime.now();

    // 一个企业可以有多个团队成员
    @JsonManagedReference
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers = new ArrayList<>();
    
    // 添加和移除团队成员的便捷方法
    public void addTeamMember(TeamMember member) {
        teamMembers.add(member);
        member.setCompany(this);
    }
    
    public void removeTeamMember(TeamMember member) {
        teamMembers.remove(member);
        member.setCompany(null);
    }
} 