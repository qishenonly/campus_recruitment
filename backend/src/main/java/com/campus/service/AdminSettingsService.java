package com.campus.service;

import com.campus.dto.ResponseDTO;
import com.campus.dto.SystemSettingDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface AdminSettingsService {
    /**
     * 获取所有系统设置
     * @return 所有系统设置
     */
    ResponseDTO<?> getAllSettings();

    /**
     * 更新所有系统设置
     * @param settings 所有系统设置
     * @return 更新结果
     */
    ResponseDTO<Void> updateAllSettings(Map<String, Object> settings);

    /**
     * 获取系统基本设置
     * @return 系统基本设置
     */
    ResponseDTO<?> getBasicSettings();

    /**
     * 更新系统基本设置
     * @param settings 系统基本设置
     * @return 更新结果
     */
    ResponseDTO<Void> updateBasicSettings(Map<String, Object> settings);

    /**
     * 上传系统LOGO
     * @param file LOGO图片
     * @return 上传结果
     */
    ResponseDTO<String> uploadLogo(MultipartFile file);

    /**
     * 获取注册设置
     * @return 注册设置
     */
    ResponseDTO<?> getRegisterSettings();

    /**
     * 更新注册设置
     * @param settings 注册设置
     * @return 更新结果
     */
    ResponseDTO<Void> updateRegisterSettings(Map<String, Object> settings);

    /**
     * 获取安全设置
     * @return 安全设置
     */
    ResponseDTO<?> getSecuritySettings();

    /**
     * 更新安全设置
     * @param settings 安全设置
     * @return 更新结果
     */
    ResponseDTO<Void> updateSecuritySettings(Map<String, Object> settings);

    /**
     * 获取系统维护状态
     * @return 系统维护状态
     */
    ResponseDTO<?> getMaintenanceMode();

    /**
     * 切换系统维护模式
     * @param enable 是否启用维护模式
     * @return 操作结果
     */
    ResponseDTO<Void> toggleMaintenanceMode(Boolean enable);

    /**
     * 获取指定组的系统设置
     * @param group 设置组
     * @return 设置信息
     */
    ResponseDTO<SystemSettingDTO> getSettingsByGroup(String group);

    /**
     * 更新指定组的系统设置
     * @param group 设置组
     * @param settingDTO 设置信息
     * @return 更新结果
     */
    ResponseDTO<Void> updateSettings(String group, SystemSettingDTO settingDTO);

    /**
     * 备份系统数据
     * @param response HTTP响应
     */
    void backupSystem(HttpServletResponse response);

    /**
     * 清理系统缓存
     * @return 操作结果
     */
    ResponseDTO<Void> clearSystemCache();

    /**
     * 重置系统
     * @param confirmation 确认文本
     * @return 操作结果
     */
    ResponseDTO<Void> resetSystem(String confirmation);
} 