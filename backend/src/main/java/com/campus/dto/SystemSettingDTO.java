package com.campus.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 系统设置DTO
 */
@Data
public class SystemSettingDTO {
    /**
     * 设置组
     */
    @NotBlank(message = "设置组不能为空")
    private String group;
    
    /**
     * 设置名称
     */
    @NotBlank(message = "设置名称不能为空")
    private String name;
    
    /**
     * 设置值
     */
    private Map<String, Object> settings;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 更新时间
     */
    private String updateTime;
} 