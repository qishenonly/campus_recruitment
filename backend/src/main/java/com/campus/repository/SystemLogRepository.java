package com.campus.repository;

import com.campus.model.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    /**
     * 根据操作类型查询日志
     * @param operationType 操作类型
     * @param pageable 分页信息
     * @return 日志分页数据
     */
    Page<SystemLog> findByOperationType(String operationType, Pageable pageable);
    
    /**
     * 根据操作者ID查询日志
     * @param operatorId 操作者ID
     * @param pageable 分页信息
     * @return 日志分页数据
     */
    Page<SystemLog> findByOperatorId(Long operatorId, Pageable pageable);
    
    /**
     * 根据操作时间范围查询日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页信息
     * @return 日志分页数据
     */
    @Query("SELECT sl FROM SystemLog sl WHERE sl.createTime >= :startTime AND sl.createTime <= :endTime")
    Page<SystemLog> findByCreateTimeBetween(
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime, 
            Pageable pageable);
    
    /**
     * 获取最近的操作日志
     * @param limit 限制数量
     * @return 日志列表
     */
    @Query("SELECT sl FROM SystemLog sl ORDER BY sl.createTime DESC")
    List<SystemLog> findRecentLogs(Pageable pageable);
} 