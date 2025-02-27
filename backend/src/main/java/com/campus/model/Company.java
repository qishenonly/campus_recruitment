package com.campus.model;

import lombok.Data;
import javax.persistence.*;

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
    
    private String location;
    
    private String website;
    
    private String logo;
    
    private Boolean verified = false;
    
    private String verificationFiles;
    
    private String contactPerson;
    
    private String contactPosition;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
} 