package com.campus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path:/files/resumes}")
    private String accessPath;

    @PostConstruct
    public void init() {
        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    log.info("成功创建上传目录: {}", directory.getAbsolutePath());
                } else {
                    log.error("无法创建上传目录: {}", directory.getAbsolutePath());
                }
            } else {
                log.info("上传目录已存在: {}", directory.getAbsolutePath());
            }
            
            // 确保目录可写
            if (!directory.canWrite()) {
                log.error("上传目录没有写入权限: {}", directory.getAbsolutePath());
            }
            
            // 记录绝对路径
            log.info("上传目录绝对路径: {}", directory.getAbsolutePath());
        } catch (Exception e) {
            log.error("初始化上传目录时出错", e);
            throw new RuntimeException("无法初始化上传目录", e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加资源处理器，使上传的文件可以通过URL访问
        registry.addResourceHandler(accessPath + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
        
        log.info("已添加资源处理器: {} -> {}", accessPath, uploadDir);
    }
} 