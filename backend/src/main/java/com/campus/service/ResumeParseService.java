package com.campus.service;

import org.springframework.web.multipart.MultipartFile;
import com.campus.model.Resume;

import java.io.IOException;
import java.util.Optional;

public interface ResumeParseService {
    /**
     * 上传并解析简历
     * @param file PDF简历文件
     * @param studentId 学生ID
     * @return 解析后的简历数据
     * @throws IOException 当文件处理出错时
     */
    Resume uploadAndParseResume(MultipartFile file, Long studentId) throws IOException;
    
    /**
     * 上传并解析简历(带名称)
     * @param file PDF简历文件
     * @param studentId 学生ID
     * @param resumeName 简历名称
     * @return 解析后的简历数据
     * @throws IOException 当文件处理出错时
     */
    default Resume uploadAndParseResume(MultipartFile file, Long studentId, String resumeName) throws IOException {
        // 默认实现，调用不带名称的方法
        return uploadAndParseResume(file, studentId);
    }
    
    /**
     * 获取简历PDF文件
     * @param resumeId 简历ID
     * @return PDF文件的字节数组
     * @throws IOException 当文件读取出错时
     */
    byte[] getResumePdf(Long resumeId) throws IOException;
    
    /**
     * 更新简历状态
     * @param resumeId 简历ID
     * @param status 新状态
     * @return 更新后的简历
     */
    Resume updateStatus(Long resumeId, String status);
} 