package com.campus.service;

import com.campus.dto.PasswordChangeDTO;
import com.campus.dto.StudentSettingsDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StudentSettingsService {
    /**
     * 获取学生个人信息
     * @param userId 用户ID
     * @return 学生个人信息DTO
     */
    StudentSettingsDTO getStudentProfile(Long userId);
    
    /**
     * 更新学生个人信息
     * @param userId 用户ID
     * @param profileDTO 学生个人信息DTO
     * @return 更新后的学生个人信息
     */
    StudentSettingsDTO updateStudentProfile(Long userId, StudentSettingsDTO profileDTO);
    
    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     * @throws IOException 文件处理异常
     */
    String uploadStudentAvatar(Long userId, MultipartFile file) throws IOException;
    
    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param passwordDTO 密码修改DTO
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, PasswordChangeDTO passwordDTO);
    
    /**
     * 绑定手机号
     * @param userId 用户ID
     * @param phone 手机号
     * @param code 验证码
     * @return 是否绑定成功
     */
    boolean bindPhone(Long userId, String phone, String code);
    
    /**
     * 绑定邮箱
     * @param userId 用户ID
     * @param email 邮箱
     * @param code 验证码
     * @return 是否绑定成功
     */
    boolean bindEmail(Long userId, String email, String code);
    
    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendPhoneCode(String phone);
    
    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @return 是否发送成功
     */
    boolean sendEmailCode(String email);
} 