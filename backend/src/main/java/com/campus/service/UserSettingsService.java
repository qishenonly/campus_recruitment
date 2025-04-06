package com.campus.service;

import com.campus.dto.PasswordChangeDTO;
import com.campus.dto.UserProfileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserSettingsService {
    /**
     * 获取用户个人信息
     * @param userId 用户ID
     * @return 用户个人信息DTO
     */
    UserProfileDTO getUserProfile(Long userId);
    
    /**
     * 更新用户个人信息
     * @param userId 用户ID
     * @param profileDTO 用户个人信息DTO
     * @return 更新后的用户个人信息
     */
    UserProfileDTO updateUserProfile(Long userId, UserProfileDTO profileDTO);
    
    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     * @throws IOException 文件处理异常
     */
    String uploadUserAvatar(Long userId, MultipartFile file) throws IOException;
    
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
} 