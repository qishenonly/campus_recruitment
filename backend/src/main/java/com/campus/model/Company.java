package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "companies")
public class Company {
    @Id
    private Long id;  // 关联User表的id
    
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

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    
    @Transient
    private List<TeamMember> teamMembers;
} 