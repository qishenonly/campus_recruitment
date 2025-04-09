package com.campus.config;

import com.campus.model.User;
import com.campus.model.SystemSetting;
import com.campus.repository.UserRepository;
import com.campus.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 数据初始化类，用于应用启动时初始化必要的数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemSettingRepository systemSettingRepository;

    @Override
    public void run(ApplicationArguments args) {
        initAdminAccount();
        initRegisterSettings();
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
    
    /**
     * 初始化注册设置
     */
    private void initRegisterSettings() {
        try {
            log.info("初始化注册设置...");
            
            // 定义默认的注册设置
            Map<String, Object> defaultSettings = new HashMap<>();
            defaultSettings.put("register.enableRegister", true);
            defaultSettings.put("register.allowStudentRegister", true);
            defaultSettings.put("register.allowCompanyRegister", true);
            defaultSettings.put("register.companyNeedVerify", true);
            defaultSettings.put("register.restrictStudentEmail", false);
            defaultSettings.put("register.allowedEmailDomains", "edu.cn,edu.com");
            defaultSettings.put("register.passwordMinLength", "8");
            defaultSettings.put("register.passwordComplexity", "uppercase,lowercase,number");
            
            // 保存或更新设置
            boolean settingsInitialized = false;
            for (Map.Entry<String, Object> entry : defaultSettings.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                
                // 检查设置是否已存在
                Optional<SystemSetting> existingSetting = systemSettingRepository.findBySettingKey(key);
                if (existingSetting.isPresent()) {
                    log.debug("设置 {} 已存在，值为: {}", key, existingSetting.get().getSettingValue());
                } else {
                    // 创建新设置
                    SystemSetting setting = new SystemSetting();
                    setting.setSettingKey(key);
                    setting.setSettingValue(value);
                    setting.setSettingGroup("register");
                    
                    // 设置描述
                    switch (key) {
                        case "register.enableRegister":
                            setting.setDescription("是否开放注册");
                            break;
                        case "register.allowStudentRegister":
                            setting.setDescription("是否允许学生注册");
                            break;
                        case "register.allowCompanyRegister":
                            setting.setDescription("是否允许企业注册");
                            break;
                        case "register.companyNeedVerify":
                            setting.setDescription("企业注册是否需要审核");
                            break;
                        case "register.restrictStudentEmail":
                            setting.setDescription("是否限制学生邮箱后缀");
                            break;
                        case "register.allowedEmailDomains":
                            setting.setDescription("允许的学生邮箱后缀列表，逗号分隔");
                            break;
                        case "register.passwordMinLength":
                            setting.setDescription("密码最小长度");
                            break;
                        case "register.passwordComplexity":
                            setting.setDescription("密码复杂度要求列表，逗号分隔");
                            break;
                    }
                    
                    systemSettingRepository.save(setting);
                    settingsInitialized = true;
                    log.debug("已创建设置: {} = {}", key, value);
                }
            }
            
            if (settingsInitialized) {
                log.info("注册设置初始化完成");
            } else {
                log.info("注册设置已存在，跳过初始化");
            }
        } catch (Exception e) {
            log.error("初始化注册设置失败: {}", e.getMessage(), e);
        }
    }
} 