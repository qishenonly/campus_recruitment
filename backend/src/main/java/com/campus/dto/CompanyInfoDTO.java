package com.campus.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompanyInfoDTO {
    private Long id;
    private String name; // 公司名称
    private String description; // 公司描述
    private String logo; // 公司logo
    private List<String> city; // 城市 (级联选择器格式) 
    private String size; // 公司规模
    private String industry; // 所属行业
    private String financingStage; // 融资阶段
    private String website; // 公司网站
    private String address; // 详细地址
    private String contactPerson; // 联系人
    private String contactPosition; // 联系人职位
    private String email; // 联系邮箱
    private String phone; // 联系电话
} 