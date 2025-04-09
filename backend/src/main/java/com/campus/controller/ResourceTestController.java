package com.campus.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 资源测试控制器
 * 用于验证静态资源配置是否正常工作
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class ResourceTestController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    /**
     * 测试静态资源配置
     * @return 配置信息
     */
    @GetMapping("/resource-config")
    public ResponseEntity<?> getResourceConfig() {
        Map<String, Object> response = new HashMap<>();
        
        // 上传目录信息
        File directory = new File(uploadDir);
        response.put("uploadDir", uploadDir);
        response.put("uploadDirExists", directory.exists());
        response.put("uploadDirCanRead", directory.canRead());
        response.put("uploadDirAbsolutePath", directory.getAbsolutePath());
        
        // 头像目录信息
        File avatarDir = new File(uploadDir, "user/avatar");
        response.put("avatarDir", avatarDir.getPath());
        response.put("avatarDirExists", avatarDir.exists());
        response.put("avatarDirCanRead", avatarDir.canRead());
        
        // 获取头像目录下的文件列表
        if (avatarDir.exists() && avatarDir.isDirectory()) {
            File[] files = avatarDir.listFiles();
            if (files != null) {
                String[] fileNames = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                response.put("avatarFiles", fileNames);
            }
        }
        
        // 访问路径配置
        response.put("accessPath", accessPath);
        response.put("fullAvatarAccessPath", accessPath + "/user/avatar");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试头像URL访问
     * @return 测试URL信息
     */
    @GetMapping("/test-avatar-url")
    public ResponseEntity<?> testAvatarUrl() {
        Map<String, Object> response = new HashMap<>();
        
        // 测试URL格式
        String testFileName = "test-avatar-" + UUID.randomUUID().toString() + ".png";
        
        // 生成不同格式的URL
        String urlWithoutApi = "/files/resumes/user/avatar/" + testFileName;
        String urlWithApi = "/api/files/resumes/user/avatar/" + testFileName;
        
        // 生成完整的外部访问URL
        String baseUrl = "http://localhost:8080"; // 这里应该是实际的服务器基础URL
        String fullUrlWithoutApi = baseUrl + urlWithoutApi;
        String fullUrlWithApi = baseUrl + urlWithApi;
        
        // 添加到响应中
        response.put("testFileName", testFileName);
        response.put("urlWithoutApi", urlWithoutApi);
        response.put("urlWithApi", urlWithApi);
        response.put("fullUrlWithoutApi", fullUrlWithoutApi);
        response.put("fullUrlWithApi", fullUrlWithApi);
        
        return ResponseEntity.ok(response);
    }
} 