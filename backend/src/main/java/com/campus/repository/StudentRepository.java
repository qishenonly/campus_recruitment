package com.campus.repository;

import com.campus.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // 直接使用id查询，因为student的id就是user的id
    
    /**
     * 统计创建时间在指定日期之前的学生数量
     * @param dateTime 指定日期
     * @return 学生数量
     */
    @Query("SELECT COUNT(s) FROM Student s JOIN s.user u WHERE u.createTime < :dateTime")
    long countByCreateTimeBefore(@Param("dateTime") LocalDateTime dateTime);
    
    /**
     * 统计创建时间在指定日期范围内的学生数量
     * @param startDateTime 开始日期
     * @param endDateTime 结束日期
     * @return 学生数量
     */
    @Query("SELECT COUNT(s) FROM Student s JOIN s.user u WHERE u.createTime >= :startDateTime AND u.createTime < :endDateTime")
    long countByCreateTimeBetween(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
} 