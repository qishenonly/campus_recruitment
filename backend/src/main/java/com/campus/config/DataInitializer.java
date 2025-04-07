package com.campus.config;

import com.campus.model.User;
import com.campus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化类，用于应用启动时初始化必要的数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initAdminAccount();
    }

    /**
     * 初始化管理员账号
     */
    private void initAdminAccount() {
        try {
            // 检查是否已存在管理员账号
            if (!userRepository.existsByUsername("admin")) {
                log.info("初始化管理员账号...");
                
                // 创建管理员用户对象
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setEmail("admin@campus.com");
                adminUser.setPhone("13800000000");
                adminUser.setRole(User.UserRole.ADMIN);
                adminUser.setStatus(User.UserStatus.ACTIVE);
                adminUser.setCreateTime(LocalDateTime.now());
                adminUser.setUpdateTime(LocalDateTime.now());
                
                // 保存到数据库
                userRepository.save(adminUser);
                
                log.info("管理员账号初始化完成");
            } else {
                log.info("管理员账号已存在，跳过初始化");
            }
        } catch (Exception e) {
            log.error("初始化管理员账号失败: {}", e.getMessage(), e);
        }
    }
} 