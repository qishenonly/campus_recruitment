package com.campus.repository;

import com.campus.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    /**
     * 根据用户名查找管理员
     * @param username 用户名
     * @return 管理员
     */
    Optional<Admin> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据邮箱查找管理员
     * @param email 邮箱
     * @return 管理员
     */
    Optional<Admin> findByEmail(String email);
} 