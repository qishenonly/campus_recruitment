package com.campus.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "system_settings")
public class SystemSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String settingKey;
    
    @Column(columnDefinition = "TEXT")
    private String settingValue;
    
    @Column(nullable = false)
    private String settingGroup;
    
    private String description;
} 