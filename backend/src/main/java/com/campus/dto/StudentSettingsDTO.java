package com.campus.dto;

import lombok.Data;

@Data
public class StudentSettingsDTO {
    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String email;
    private String phone;
    private String university;
    private String major;
    private String education;
    private String graduationYear;
    private String location;
    private String introducation;
} 