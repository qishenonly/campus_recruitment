package com.campus.service.impl;

import com.campus.model.SystemLog;
import com.campus.repository.SystemLogRepository;
import com.campus.service.SystemLogService;
import com.campus.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public SystemLog logOperation(String operationType, String content, Long operatorId, String operatorName, HttpServletRequest request) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setOperationType(operationType);
            systemLog.setContent(content);
            systemLog.setOperatorId(operatorId);
            systemLog.setOperatorName(operatorName);
            systemLog.setIpAddress(getClientIp(request));
            systemLog.setCreateTime(LocalDateTime.now());
            
            return systemLogRepository.save(systemLog);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional
    public SystemLog logOperation(String operationType, String content, String token, HttpServletRequest request) {
        try {
            // 从JWT令牌中提取用户信息
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            String username = jwtUtil.getUsernameFromToken(jwtToken);
            
            return logOperation(operationType, content, userId, username, request);
        } catch (Exception e) {
            log.error("从令牌记录操作日志失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Page<SystemLog> queryLogs(String operationType, Long operatorId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        // 构建排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        
        // 根据条件查询
        if (operationType != null && !operationType.isEmpty()) {
            return systemLogRepository.findByOperationType(operationType, sortedPageable);
        } else if (operatorId != null) {
            return systemLogRepository.findByOperatorId(operatorId, sortedPageable);
        } else if (startTime != null && endTime != null) {
            return systemLogRepository.findByCreateTimeBetween(startTime, endTime, sortedPageable);
        } else {
            // 默认查询所有并按时间排序
            return systemLogRepository.findAll(sortedPageable);
        }
    }

    @Override
    public List<SystemLog> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createTime"));
        return systemLogRepository.findRecentLogs(pageable);
    }

    @Override
    public SystemLog getLogById(Long id) {
        return systemLogRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public int cleanupExpiredLogs(int keepDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(keepDays);
            List<SystemLog> expiredLogs = systemLogRepository.findByCreateTimeBetween(
                    LocalDateTime.MIN, cutoffDate, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
                    
            if (!expiredLogs.isEmpty()) {
                systemLogRepository.deleteAll(expiredLogs);
                return expiredLogs.size();
            }
            return 0;
        } catch (Exception e) {
            log.error("清理过期日志失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 获取客户端IP地址
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 