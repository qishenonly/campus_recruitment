package com.campus.controller.admin;

import com.campus.annotation.OperationLog;
import com.campus.dto.ResponseDTO;
import com.campus.dto.SystemSettingDTO;
import com.campus.service.AdminSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    @Autowired
    private AdminSettingsService adminSettingsService;

    /**
     * 获取所有系统设置
     * @return 所有系统设置
     */
    @GetMapping
    public ResponseDTO<?> getAllSettings() {
        return adminSettingsService.getAllSettings();
    }

    /**
     * 更新所有系统设置
     * @param settings 所有系统设置
     * @return 更新结果
     */
    @PutMapping
    @OperationLog(operationType = "系统设置", description = "更新所有系统设置")
    public ResponseDTO<Void> updateAllSettings(@RequestBody Map<String, Object> settings) {
        return adminSettingsService.updateAllSettings(settings);
    }

    /**
     * 获取系统基本设置
     * @return 系统基本设置
     */
    @GetMapping("/basic")
    public ResponseDTO<?> getBasicSettings() {
        return adminSettingsService.getBasicSettings();
    }

    /**
     * 更新系统基本设置
     * @param settings 系统基本设置
     * @return 更新结果
     */
    @PutMapping("/basic")
    @OperationLog(operationType = "系统设置", description = "更新基本设置")
    public ResponseDTO<Void> updateBasicSettings(@RequestBody Map<String, Object> settings) {
        return adminSettingsService.updateBasicSettings(settings);
    }

    /**
     * 上传系统LOGO
     * @param file LOGO图片
     * @return 上传结果
     */
    @PostMapping("/logo")
    @OperationLog(operationType = "系统设置", description = "上传系统LOGO")
    public ResponseDTO<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        return adminSettingsService.uploadLogo(file);
    }

    /**
     * 获取注册设置
     * @return 注册设置
     */
    @GetMapping("/register")
    public ResponseDTO<?> getRegisterSettings() {
        return adminSettingsService.getRegisterSettings();
    }

    /**
     * 更新注册设置
     * @param settings 注册设置
     * @return 更新结果
     */
    @PutMapping("/register")
    public ResponseDTO<Void> updateRegisterSettings(@RequestBody Map<String, Object> settings) {
        return adminSettingsService.updateRegisterSettings(settings);
    }

    /**
     * 获取安全设置
     * @return 安全设置
     */
    @GetMapping("/security")
    public ResponseDTO<?> getSecuritySettings() {
        return adminSettingsService.getSecuritySettings();
    }

    /**
     * 更新安全设置
     * @param settings 安全设置
     * @return 更新结果
     */
    @PutMapping("/security")
    public ResponseDTO<Void> updateSecuritySettings(@RequestBody Map<String, Object> settings) {
        return adminSettingsService.updateSecuritySettings(settings);
    }

    /**
     * 获取邮件设置
     * @return 邮件设置
     */
    @GetMapping("/mail")
    public ResponseDTO<?> getMailSettings() {
        return adminSettingsService.getMailSettings();
    }

    /**
     * 更新邮件设置
     * @param settings 邮件设置
     * @return 更新结果
     */
    @PutMapping("/mail")
    @OperationLog(operationType = "系统设置", description = "更新邮件设置")
    public ResponseDTO<Void> updateMailSettings(@RequestBody Map<String, Object> settings) {
        return adminSettingsService.updateMailSettings(settings);
    }

    /**
     * 发送测试邮件
     * @param testMail 测试邮件地址
     * @return 发送结果
     */
    @PostMapping("/mail/test")
    @OperationLog(operationType = "系统设置", description = "发送测试邮件")
    public ResponseDTO<Void> sendTestMail(@RequestBody Map<String, String> testMail) {
        return adminSettingsService.sendTestMail(testMail.get("email"));
    }

    /**
     * 获取系统维护状态
     * @return 系统维护状态
     */
    @GetMapping("/maintenance")
    public ResponseDTO<?> getMaintenanceMode() {
        return adminSettingsService.getMaintenanceMode();
    }

    /**
     * 切换系统维护模式
     * @param enable 是否启用维护模式
     * @return 操作结果
     */
    @PutMapping("/maintenance")
    @OperationLog(operationType = "系统设置", description = "切换系统维护模式")
    public ResponseDTO<Void> toggleMaintenanceMode(@RequestParam("enable") Boolean enable) {
        return adminSettingsService.toggleMaintenanceMode(enable);
    }

    /**
     * 获取指定组的系统设置
     * @param group 设置组
     * @return 设置组
     */
    @GetMapping("/group/{group}")
    public ResponseDTO<SystemSettingDTO> getSettingsByGroup(@PathVariable("group") String group) {
        return adminSettingsService.getSettingsByGroup(group);
    }

    /**
     * 更新指定组的系统设置
     * @param group 设置组
     * @param settingDTO 设置信息
     * @return 更新结果
     */
    @PutMapping("/group/{group}")
    public ResponseDTO<Void> updateSettings(
            @PathVariable("group") String group,
            @RequestBody SystemSettingDTO settingDTO) {
        return adminSettingsService.updateSettings(group, settingDTO);
    }
    
    /**
     * 备份系统数据
     * @param response HTTP响应
     */
    @GetMapping("/backup")
    public void backupSystem(HttpServletResponse response) {
        adminSettingsService.backupSystem(response);
    }

    /**
     * 清理系统缓存
     * @return 清理结果
     */
    @PostMapping("/cache/clear")
    @OperationLog(operationType = "系统设置", description = "清理系统缓存")
    public ResponseDTO<Void> clearSystemCache() {
        return adminSettingsService.clearSystemCache();
    }

    /**
     * 重置系统
     * @param confirmation 确认文本
     * @return 重置结果
     */
    @PostMapping("/reset")
    @OperationLog(operationType = "系统设置", description = "重置系统")
    public ResponseDTO<Void> resetSystem(@RequestParam("confirmation") String confirmation) {
        return adminSettingsService.resetSystem(confirmation);
    }
} 