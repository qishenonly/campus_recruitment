package com.campus.service.impl;

import com.campus.dto.ResponseDTO;
import com.campus.dto.SystemSettingDTO;
import com.campus.model.SystemSetting;
import com.campus.repository.SystemSettingRepository;
import com.campus.service.AdminSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AdminSettingsServiceImpl implements AdminSettingsService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Override
    public ResponseDTO<?> getAllSettings() {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        
        Map<String, Map<String, Object>> result = settings.stream()
                .collect(Collectors.groupingBy(
                        SystemSetting::getSettingGroup,
                        Collectors.toMap(
                                SystemSetting::getSettingKey,
                                setting -> {
                                    Map<String, Object> settingMap = new HashMap<>();
                                    settingMap.put("value", setting.getSettingValue());
                                    settingMap.put("description", setting.getDescription());
                                    return settingMap;
                                }
                        )
                ));
        
        return ResponseDTO.success(result);
    }

    @Override
    @Transactional
    public ResponseDTO<Void> updateAllSettings(Map<String, Object> settings) {
        settings.forEach((key, value) -> {
            Optional<SystemSetting> settingOpt = systemSettingRepository.findBySettingKey(key);
            
            if (settingOpt.isPresent()) {
                SystemSetting setting = settingOpt.get();
                setting.setSettingValue(value.toString());
                systemSettingRepository.save(setting);
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    public ResponseDTO<?> getBasicSettings() {
        List<SystemSetting> settings = systemSettingRepository.findBySettingGroup("basic");
        
        Map<String, Object> result = settings.stream()
                .collect(Collectors.toMap(
                        SystemSetting::getSettingKey,
                        SystemSetting::getSettingValue
                ));
        
        return ResponseDTO.success(result);
    }

    @Override
    @Transactional
    public ResponseDTO<Void> updateBasicSettings(Map<String, Object> settings) {
        settings.forEach((key, value) -> {
            Optional<SystemSetting> settingOpt = systemSettingRepository.findBySettingKey(key);
            
            if (settingOpt.isPresent()) {
                SystemSetting setting = settingOpt.get();
                setting.setSettingValue(value.toString());
                systemSettingRepository.save(setting);
            } else if (key.startsWith("basic.")) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(key);
                setting.setSettingValue(value.toString());
                setting.setSettingGroup("basic");
                systemSettingRepository.save(setting);
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    public ResponseDTO<String> uploadLogo(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseDTO.error("上传文件不能为空");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseDTO.error("只能上传图片文件");
        }
        
        try {
            // 创建目录
            String logoDir = uploadPath + "/logo";
            Path logoPath = Paths.get(logoDir);
            if (!Files.exists(logoPath)) {
                Files.createDirectories(logoPath);
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".png";
                    
            String fileName = "logo_" + System.currentTimeMillis() + fileExtension;
            String filePath = logoDir + "/" + fileName;
            
            // 保存文件
            file.transferTo(new File(filePath));
            
            // 更新设置
            Optional<SystemSetting> logoSettingOpt = systemSettingRepository.findBySettingKey("basic.logo");
            if (logoSettingOpt.isPresent()) {
                SystemSetting logoSetting = logoSettingOpt.get();
                logoSetting.setSettingValue("/logo/" + fileName);
                systemSettingRepository.save(logoSetting);
            } else {
                SystemSetting logoSetting = new SystemSetting();
                logoSetting.setSettingKey("basic.logo");
                logoSetting.setSettingValue("/logo/" + fileName);
                logoSetting.setSettingGroup("basic");
                logoSetting.setDescription("系统LOGO");
                systemSettingRepository.save(logoSetting);
            }
            
            return ResponseDTO.success("/logo/" + fileName);
        } catch (IOException e) {
            return ResponseDTO.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<?> getRegisterSettings() {
        List<SystemSetting> settings = systemSettingRepository.findBySettingGroup("register");
        
        Map<String, Object> result = settings.stream()
                .collect(Collectors.toMap(
                        SystemSetting::getSettingKey,
                        setting -> {
                            // 对于布尔值进行特殊处理
                            if (setting.getSettingValue().equalsIgnoreCase("true") || 
                                setting.getSettingValue().equalsIgnoreCase("false")) {
                                return Boolean.parseBoolean(setting.getSettingValue());
                            }
                            return setting.getSettingValue();
                        }
                ));
        
        return ResponseDTO.success(result);
    }

    @Override
    @Transactional
    public ResponseDTO<Void> updateRegisterSettings(Map<String, Object> settings) {
        settings.forEach((key, value) -> {
            Optional<SystemSetting> settingOpt = systemSettingRepository.findBySettingKey(key);
            
            if (settingOpt.isPresent()) {
                SystemSetting setting = settingOpt.get();
                setting.setSettingValue(value.toString());
                systemSettingRepository.save(setting);
            } else if (key.startsWith("register.")) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(key);
                setting.setSettingValue(value.toString());
                setting.setSettingGroup("register");
                systemSettingRepository.save(setting);
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    public ResponseDTO<?> getSecuritySettings() {
        List<SystemSetting> settings = systemSettingRepository.findBySettingGroup("security");
        
        Map<String, Object> result = settings.stream()
                .collect(Collectors.toMap(
                        SystemSetting::getSettingKey,
                        setting -> {
                            // 对于布尔值进行特殊处理
                            if (setting.getSettingValue().equalsIgnoreCase("true") || 
                                setting.getSettingValue().equalsIgnoreCase("false")) {
                                return Boolean.parseBoolean(setting.getSettingValue());
                            }
                            return setting.getSettingValue();
                        }
                ));
        
        return ResponseDTO.success(result);
    }

    @Override
    @Transactional
    public ResponseDTO<Void> updateSecuritySettings(Map<String, Object> settings) {
        settings.forEach((key, value) -> {
            Optional<SystemSetting> settingOpt = systemSettingRepository.findBySettingKey(key);
            
            if (settingOpt.isPresent()) {
                SystemSetting setting = settingOpt.get();
                setting.setSettingValue(value.toString());
                systemSettingRepository.save(setting);
            } else if (key.startsWith("security.")) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(key);
                setting.setSettingValue(value.toString());
                setting.setSettingGroup("security");
                systemSettingRepository.save(setting);
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    public ResponseDTO<?> getMaintenanceMode() {
        Optional<SystemSetting> maintenanceSetting = systemSettingRepository.findBySettingKey("system.maintenance");
        
        boolean maintenance = maintenanceSetting.isPresent() && 
                              Boolean.parseBoolean(maintenanceSetting.get().getSettingValue());
        
        Map<String, Object> result = new HashMap<>();
        result.put("maintenance", maintenance);
        
        return ResponseDTO.success(result);
    }

    @Override
    @Transactional
    public ResponseDTO<Void> toggleMaintenanceMode(Boolean enable) {
        Optional<SystemSetting> maintenanceSetting = systemSettingRepository.findBySettingKey("system.maintenance");
        
        if (maintenanceSetting.isPresent()) {
            SystemSetting setting = maintenanceSetting.get();
            setting.setSettingValue(enable.toString());
            systemSettingRepository.save(setting);
        } else {
            SystemSetting setting = new SystemSetting();
            setting.setSettingKey("system.maintenance");
            setting.setSettingValue(enable.toString());
            setting.setSettingGroup("system");
            setting.setDescription("系统维护模式");
            systemSettingRepository.save(setting);
        }
        
        return ResponseDTO.success();
    }

    @Override
    public void backupSystem(HttpServletResponse response) {
        try {
            // 设置响应头
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "system_backup_" + timestamp + ".zip";
            
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            
            // 创建ZIP输出流
            ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
            
            // 添加系统设置到ZIP
            addSystemSettingsToZip(zipOut);
            
            // 添加上传目录到ZIP
            addDirectoryToZip(new File(uploadPath), zipOut, "uploads/");
            
            zipOut.close();
        } catch (IOException e) {
            throw new RuntimeException("备份系统数据失败: " + e.getMessage());
        }
    }

    private void addSystemSettingsToZip(ZipOutputStream zipOut) throws IOException {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        
        StringBuilder sb = new StringBuilder();
        sb.append("id,setting_key,setting_value,setting_group,description\n");
        
        for (SystemSetting setting : settings) {
            sb.append(setting.getId()).append(",");
            sb.append(setting.getSettingKey()).append(",");
            sb.append(setting.getSettingValue()).append(",");
            sb.append(setting.getSettingGroup()).append(",");
            sb.append(setting.getDescription()).append("\n");
        }
        
        ZipEntry settingsEntry = new ZipEntry("settings.csv");
        zipOut.putNextEntry(settingsEntry);
        zipOut.write(sb.toString().getBytes());
        zipOut.closeEntry();
    }

    private void addDirectoryToZip(File sourceDir, ZipOutputStream zipOut, String parentPath) throws IOException {
        if (sourceDir == null || !sourceDir.exists()) {
            return;
        }
        
        File[] files = sourceDir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                addDirectoryToZip(file, zipOut, parentPath + file.getName() + "/");
            } else {
                ZipEntry entry = new ZipEntry(parentPath + file.getName());
                zipOut.putNextEntry(entry);
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, length);
                    }
                }
                
                zipOut.closeEntry();
            }
        }
    }

    @Override
    @Transactional
    public ResponseDTO<Void> clearSystemCache() {
        try {
            // 清理缓存实现
            // 这里可以添加清理Redis缓存、内存缓存等操作
            
            return ResponseDTO.success();
        } catch (Exception e) {
            return ResponseDTO.error("清理缓存失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO<Void> resetSystem(String confirmation) {
        if (!"CONFIRM_RESET".equals(confirmation)) {
            return ResponseDTO.error("确认文本不正确");
        }
        
        try {
            // 重置系统实现，根据实际需求可包括：
            // 1. 清理数据库（保留必要的系统设置）
            // 2. 清理缓存
            // 3. 初始化基础数据
            
            return ResponseDTO.success();
        } catch (Exception e) {
            return ResponseDTO.error("重置系统失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<SystemSettingDTO> getSettingsByGroup(String group) {
        List<SystemSetting> settings = systemSettingRepository.findBySettingGroup(group);
        
        if (settings.isEmpty()) {
            return ResponseDTO.error("没有找到 " + group + " 组的设置");
        }
        
        SystemSettingDTO settingDTO = new SystemSettingDTO();
        settingDTO.setGroup(group);
        settingDTO.setName(group + " settings");
        
        Map<String, Object> settingMap = settings.stream()
                .collect(Collectors.toMap(
                        SystemSetting::getSettingKey,
                        setting -> {
                            // 对于布尔值进行特殊处理
                            if (setting.getSettingValue().equalsIgnoreCase("true") || 
                                setting.getSettingValue().equalsIgnoreCase("false")) {
                                return Boolean.parseBoolean(setting.getSettingValue());
                            }
                            return setting.getSettingValue();
                        }
                ));
        
        settingDTO.setSettings(settingMap);
        
        return ResponseDTO.success(settingDTO);
    }
    
    @Override
    @Transactional
    public ResponseDTO<Void> updateSettings(String group, SystemSettingDTO settingDTO) {
        if (settingDTO.getSettings() == null || settingDTO.getSettings().isEmpty()) {
            return ResponseDTO.error("设置数据不能为空");
        }
        
        settingDTO.getSettings().forEach((key, value) -> {
            Optional<SystemSetting> settingOpt = systemSettingRepository.findBySettingKey(key);
            
            if (settingOpt.isPresent()) {
                SystemSetting setting = settingOpt.get();
                setting.setSettingValue(value.toString());
                systemSettingRepository.save(setting);
            } else if (key.startsWith(group + ".")) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(key);
                setting.setSettingValue(value.toString());
                setting.setSettingGroup(group);
                systemSettingRepository.save(setting);
            }
        });
        
        return ResponseDTO.success();
    }
} 