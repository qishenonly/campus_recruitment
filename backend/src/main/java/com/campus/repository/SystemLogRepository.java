package com.campus.repository;

import com.campus.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    /**
     * 根据操作类型查找日志
     * @param operationType 操作类型
     * @return 日志列表
     */
    List<SystemLog> findByOperationType(String operationType);
    
    /**
     * 根据时间范围查找日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<SystemLog> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据操作者查找日志
     * @param operatorId 操作者ID
     * @return 日志列表
     */
    List<SystemLog> findByOperatorId(Long operatorId);
} 