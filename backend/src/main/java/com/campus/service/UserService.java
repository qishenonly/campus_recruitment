package com.campus.service;

import com.campus.model.User;
import com.campus.model.SystemSetting;
import com.campus.repository.UserRepository;
import com.campus.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SystemSettingRepository systemSettingRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByRealName(String realName) {
        return userRepository.existsByRealName(realName);
    }
    
    /**
     * 获取系统设置的布尔值
     * @param key 设置键
     * @param defaultValue 默认值
     * @return 设置值
     */
    public boolean getSystemSettingBooleanValue(String key, boolean defaultValue) {
        Optional<SystemSetting> setting = systemSettingRepository.findBySettingKey(key);
        if (setting.isPresent() && setting.get().getSettingValue() != null) {
            return Boolean.parseBoolean(setting.get().getSettingValue());
        }
        return defaultValue;
    }
} 