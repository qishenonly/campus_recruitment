package com.campus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加资源处理器，使上传的文件可以通过URL访问
        // 确保访问路径以 / 开头
        String normalizedAccessPath = accessPath.startsWith("/") ? accessPath : "/" + accessPath;
        
        // 确保上传目录以 / 结尾
        String normalizedUploadDir = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        
        // 注册资源处理器
        registry.addResourceHandler(normalizedAccessPath + "/**")
                .addResourceLocations("file:" + normalizedUploadDir);
        
        // 单独配置头像图片访问
        // 添加/api前缀的路径映射
        registry.addResourceHandler("/api/files/resumes/user/avatar/**")
                .addResourceLocations("file:" + normalizedUploadDir + "user/avatar/");
        
        // 保留原路径映射，以保证兼容性
        registry.addResourceHandler("/files/resumes/user/avatar/**")
                .addResourceLocations("file:" + normalizedUploadDir + "user/avatar/");
        
        log.info("已添加资源处理器: {} -> {}", normalizedAccessPath, normalizedUploadDir);
        log.info("已添加头像资源处理器(带API前缀): /api/files/resumes/user/avatar/** -> {}", normalizedUploadDir + "user/avatar/");
        log.info("已添加头像资源处理器(不带API前缀): /files/resumes/user/avatar/** -> {}", normalizedUploadDir + "user/avatar/");
    }
} 