package com.campus.dto;

import com.campus.model.SystemLog;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统日志DTO - 用于返回格式化的日志信息
 */
@Data
public class SystemLogDTO {
    private Long id;
    private String type; // 操作类型（简化版）
    private String action; // 具体操作
    private String content; // 简化后的操作内容
    private String description; // 人类可读的操作描述
    private String user; // 操作用户
    private String ipAddress; // IP地址
    private String time; // 格式化的时间
    private LocalDateTime createTime; // 原始时间

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // API路径到可读描述的映射
    private static final Map<String, String> API_DESCRIPTION_MAP = new HashMap<>();
    
    // URL模式匹配的正则表达式
    private static final Pattern URL_PATTERN = Pattern.compile("^(\\w+)\\s+/api/(?:admin/)?(\\w+)(?:/(\\d+))?(?:/.*)?");

    static {
        // 初始化API描述映射
        API_DESCRIPTION_MAP.put("POST /api/admin/login", "管理员登录");
        API_DESCRIPTION_MAP.put("POST /api/auth/login", "用户登录");
        API_DESCRIPTION_MAP.put("POST /api/auth/register", "用户注册");
        API_DESCRIPTION_MAP.put("POST /api/company/employee/jobs", "发布职位");
        API_DESCRIPTION_MAP.put("PUT /api/company/employee/jobs", "更新职位");
        API_DESCRIPTION_MAP.put("DELETE /api/company/employee/jobs", "删除职位");
        
        API_DESCRIPTION_MAP.put("POST /api/job-applications", "申请职位");
        API_DESCRIPTION_MAP.put("PUT /api/job-applications", "更新职位申请");
        
        API_DESCRIPTION_MAP.put("POST /api/admin/students", "添加学生");
        API_DESCRIPTION_MAP.put("PUT /api/admin/students", "更新学生信息");
        API_DESCRIPTION_MAP.put("DELETE /api/admin/students", "删除学生");
        
        API_DESCRIPTION_MAP.put("POST /api/admin/companies", "添加企业");
        API_DESCRIPTION_MAP.put("PUT /api/admin/companies", "更新企业信息");
        API_DESCRIPTION_MAP.put("DELETE /api/admin/companies", "删除企业");
        
        API_DESCRIPTION_MAP.put("POST /api/resumes", "上传简历");
        API_DESCRIPTION_MAP.put("PUT /api/resumes", "更新简历");
        API_DESCRIPTION_MAP.put("DELETE /api/resumes", "删除简历");
        
        API_DESCRIPTION_MAP.put("POST /api/conversations", "发送消息");
        API_DESCRIPTION_MAP.put("PUT /api/conversations", "更新消息");
        
        API_DESCRIPTION_MAP.put("POST /api/admin/settings", "更新系统设置");
        API_DESCRIPTION_MAP.put("PUT /api/admin/settings", "更新系统设置");
    }
    
    /**
     * 从SystemLog实体转换为DTO
     * @param log 系统日志实体
     * @return 系统日志DTO
     */
    public static SystemLogDTO fromEntity(SystemLog log) {
        if (log == null) {
            return null;
        }
        
        SystemLogDTO dto = new SystemLogDTO();
        dto.setId(log.getId());
        
        // 处理操作类型，提取主类型和具体操作
        String operationType = log.getOperationType();
        if (operationType != null && operationType.contains("-")) {
            String[] parts = operationType.split("-", 2);
            dto.setType(parts[0].trim());
            dto.setAction(parts[1].trim());
        } else {
            dto.setType(operationType);
            dto.setAction("");
        }
        
        // 处理内容字段，简化内容显示
        String content = log.getContent();
        if (content != null) {
            // 从内容中提取HTTP方法和路径
            String apiPath = extractApiPath(content);
            String readableDescription = getReadableDescription(apiPath, content);
            
            // 设置可读描述
            dto.setDescription(readableDescription);
            
            // 简化内容，只保留关键信息
            if (content.contains(", 参数:")) {
                content = content.substring(0, content.indexOf(", 参数:"));
            }
            dto.setContent(content);
        }
        
        // 设置用户信息
        dto.setUser(log.getOperatorName());
        dto.setIpAddress(log.getIpAddress());
        
        // 格式化时间
        LocalDateTime createTime = log.getCreateTime();
        dto.setCreateTime(createTime);
        if (createTime != null) {
            dto.setTime(createTime.format(DATE_FORMATTER));
        }
        
        return dto;
    }
    
    /**
     * 从内容中提取API路径
     * @param content 内容
     * @return API路径
     */
    private static String extractApiPath(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // 提取HTTP方法和路径
        Matcher matcher = URL_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1) + " /api/" + matcher.group(2);
        }
        
        // 如果无法从正则匹配提取，尝试简单提取
        int commaIndex = content.indexOf(", ");
        if (commaIndex > 0) {
            return content.substring(0, commaIndex);
        }
        
        return content;
    }
    
    /**
     * 获取更可读的操作描述
     * @param apiPath API路径
     * @param content 原始内容
     * @return 可读描述
     */
    private static String getReadableDescription(String apiPath, String content) {
        // 首先检查是否有预定义的描述
        if (API_DESCRIPTION_MAP.containsKey(apiPath)) {
            return API_DESCRIPTION_MAP.get(apiPath);
        }
        
        // 尝试从内容中提取更可读的描述
        if (content.contains("前台接口调用")) {
            return "前台用户操作";
        } else if (content.contains("管理后台接口调用")) {
            return "管理后台操作";
        }
        
        // 基于HTTP方法和资源类型生成描述
        Matcher matcher = URL_PATTERN.matcher(content);
        if (matcher.find()) {
            String httpMethod = matcher.group(1);
            String resource = matcher.group(2);
            
            String action = getActionByHttpMethod(httpMethod);
            String resourceName = getResourceName(resource);
            
            return action + resourceName;
        }
        
        // 如果都无法识别，返回简化后的内容
        return content.length() > 30 ? content.substring(0, 30) + "..." : content;
    }
    
    /**
     * 根据HTTP方法获取动作描述
     * @param httpMethod HTTP方法
     * @return 动作描述
     */
    private static String getActionByHttpMethod(String httpMethod) {
        switch (httpMethod) {
            case "POST": return "创建";
            case "PUT": return "更新";
            case "DELETE": return "删除";
            case "PATCH": return "修改";
            default: return "操作";
        }
    }
    
    /**
     * 获取资源的中文名称
     * @param resource 资源标识
     * @return 资源名称
     */
    private static String getResourceName(String resource) {
        switch (resource.toLowerCase()) {
            case "jobs": return "职位";
            case "students": return "学生";
            case "companies": return "企业";
            case "resumes": return "简历";
            case "applications": return "申请";
            case "conversations": return "消息";
            case "settings": return "设置";
            case "auth": return "认证";
            case "admin": return "管理员";
            case "users": return "用户";
            default: return resource;
        }
    }
} 