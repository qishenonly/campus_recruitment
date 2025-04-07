package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_logs")
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String operationType;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private String operatorName;
    
    private Long operatorId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
} 