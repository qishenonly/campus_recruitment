package com.campus.controller.admin;

import com.campus.dto.ResponseDTO;
import com.campus.dto.PageDTO;
import com.campus.dto.SystemLogDTO;
import com.campus.model.SystemLog;
import com.campus.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统日志控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/logs")
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * 分页查询系统日志
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param operationType 操作类型
     * @param operatorId 操作者ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志分页数据
     */
    @GetMapping
    public ResponseDTO<PageDTO<SystemLogDTO>> getLogs(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "type", required = false) String operationType,
            @RequestParam(value = "operator", required = false) Long operatorId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Page<SystemLog> logPage = systemLogService.queryLogs(
                    operationType, operatorId, startTime, endTime, pageable);
            
            // 转换为DTO
            List<SystemLogDTO> formattedLogs = logPage.getContent().stream()
                .map(SystemLogDTO::fromEntity)
                .collect(Collectors.toList());
            
            PageDTO<SystemLogDTO> pageDTO = new PageDTO<>();
            pageDTO.setList(formattedLogs);
            pageDTO.setTotal(logPage.getTotalElements());
            pageDTO.setPages(logPage.getTotalPages());
            pageDTO.setPageNum(pageNum);
            pageDTO.setPageSize(pageSize);
            
            return ResponseDTO.success(pageDTO);
        } catch (Exception e) {
            log.error("查询系统日志失败", e);
            return ResponseDTO.error("查询系统日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志详情
     * @param id 日志ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public ResponseDTO<SystemLogDTO> getLogDetail(@PathVariable Long id) {
        try {
            SystemLog log = systemLogService.getLogById(id);
            if (log == null) {
                return ResponseDTO.error("日志不存在");
            }
            
            // 转换为DTO
            SystemLogDTO logDTO = SystemLogDTO.fromEntity(log);
            return ResponseDTO.success(logDTO);
        } catch (Exception e) {
            log.error("获取日志详情失败", e);
            return ResponseDTO.error("获取日志详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近的操作日志
     * @param limit 限制数量
     * @return 日志列表
     */
    @GetMapping("/recent")
    public ResponseDTO<List<SystemLogDTO>> getRecentLogs(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        try {
            List<SystemLog> logs = systemLogService.getRecentLogs(limit);
            
            // 转换为DTO
            List<SystemLogDTO> formattedLogs = logs.stream()
                .map(SystemLogDTO::fromEntity)
                .collect(Collectors.toList());
                
            return ResponseDTO.success(formattedLogs);
        } catch (Exception e) {
            log.error("获取最近操作日志失败", e);
            return ResponseDTO.error("获取最近操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期日志
     * @param days 保留天数
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    public ResponseDTO<Map<String, Object>> cleanupLogs(
            @RequestParam(value = "days", defaultValue = "90") Integer days) {
        try {
            int count = systemLogService.cleanupExpiredLogs(days);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("message", String.format("成功清理%d条过期日志", count));
            
            return ResponseDTO.success(result);
        } catch (Exception e) {
            log.error("清理过期日志失败", e);
            return ResponseDTO.error("清理过期日志失败: " + e.getMessage());
        }
    }
} 