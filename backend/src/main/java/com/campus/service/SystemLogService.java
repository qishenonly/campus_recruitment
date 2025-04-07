package com.campus.service;

import com.campus.model.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface SystemLogService {
    
    /**
     * 记录系统操作日志
     * @param operationType 操作类型
     * @param content 操作内容
     * @param operatorId 操作者ID
     * @param operatorName 操作者名称
     * @param request HTTP请求对象，用于获取IP
     * @return 创建的日志对象
     */
    SystemLog logOperation(String operationType, String content, Long operatorId, String operatorName, HttpServletRequest request);
    
    /**
     * 简化的记录日志方法，从令牌中获取操作者信息
     * @param operationType 操作类型
     * @param content 操作内容
     * @param token JWT令牌
     * @param request HTTP请求对象
     * @return 创建的日志对象
     */
    SystemLog logOperation(String operationType, String content, String token, HttpServletRequest request);
    
    /**
     * 分页查询日志
     * @param operationType 操作类型
     * @param operatorId 操作者ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页对象
     * @return 日志分页数据
     */
    Page<SystemLog> queryLogs(String operationType, Long operatorId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 获取最近的操作日志
     * @param limit 限制数量
     * @return 日志列表
     */
    List<SystemLog> getRecentLogs(int limit);
    
    /**
     * 按ID获取日志详情
     * @param id 日志ID
     * @return 日志对象
     */
    SystemLog getLogById(Long id);
    
    /**
     * 清理过期日志（可选）
     * @param keepDays 保留天数
     * @return 清理的记录数
     */
    int cleanupExpiredLogs(int keepDays);
} 