package com.campus.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "students")
public class Student {
    @Id
    private Long id;  // 这个id是users表的id
    
    @Column(nullable = false)
    private String realName;
    
    @Column(nullable = false)
    private String university;
    
    @Column(nullable = false)
    private String major;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Education education;
    
    @Column(nullable = false)
    private String graduationYear;
    
    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.保密;
    
    private LocalDate birth;
    
    private String location;
    
    private String expectedPosition;
    
    private String expectedSalary;
    
    private String expectedCity;
    
    @OneToOne
    @MapsId  // 使用user的id作为student的id
    @JoinColumn(name = "id")
    private User user;
    
    public enum Education {
        专科, 本科, 硕士, 博士
    }
    
    public enum Gender {
        男, 女, 保密
    }
} 