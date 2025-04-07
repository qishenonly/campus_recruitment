package com.campus.repository;

import com.campus.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    /**
     * 根据设置键查找设置
     * @param settingKey 设置键
     * @return 设置
     */
    Optional<SystemSetting> findBySettingKey(String settingKey);
    
    /**
     * 根据设置组查找设置列表
     * @param settingGroup 设置组
     * @return 设置列表
     */
    List<SystemSetting> findBySettingGroup(String settingGroup);
    
    /**
     * 根据设置键检查设置是否存在
     * @param settingKey 设置键
     * @return 是否存在
     */
    boolean existsBySettingKey(String settingKey);
} 