package com.campus.service.impl;

import com.campus.dto.PasswordChangeDTO;
import com.campus.dto.UserProfileDTO;
import com.campus.model.User;
import com.campus.repository.UserRepository;
import com.campus.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        log.info("获取用户ID={}的个人信息", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        return convertToDTO(user);
    }

    @Override
    public UserProfileDTO updateUserProfile(Long userId, UserProfileDTO profileDTO) {
        log.info("更新用户ID={}的个人信息", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 更新用户信息
        user.setRealName(profileDTO.getRealName());
        
        // 如果修改了用户名，检查是否唯一
        if (!user.getUsername().equals(profileDTO.getUsername())) {
            if (userRepository.existsByUsername(profileDTO.getUsername())) {
                throw new IllegalArgumentException("该用户名已被使用");
            }
            user.setUsername(profileDTO.getUsername());
        }
        
        // 如果修改了邮箱，检查是否唯一
        if (!user.getEmail().equals(profileDTO.getEmail())) {
            if (userRepository.existsByEmail(profileDTO.getEmail())) {
                throw new IllegalArgumentException("该邮箱已被使用");
            }
            user.setEmail(profileDTO.getEmail());
        }
        
        // 更新手机号
        user.setPhone(profileDTO.getPhone());
        
        // 更新时间
        user.setUpdateTime(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        log.info("用户个人信息更新成功");
        
        return convertToDTO(updatedUser);
    }

    @Override
    public String uploadUserAvatar(Long userId, MultipartFile file) throws IOException {
        log.info("上传用户ID={}的头像", userId);
        
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("只支持jpg和png格式的图片");
        }
        
        // 确保目录存在
        String avatarDir = uploadDir + "/user/avatar";
        File directory = new File(avatarDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("无法创建上传目录");
            }
        }
        
        // 生成唯一文件名
        String extension = contentType.equals("image/jpeg") ? ".jpg" : ".png";
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = Paths.get(avatarDir, fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 构建访问URL - 直接使用我们在StaticResourceConfig中配置的路径
        // 使用固定格式的绝对路径，确保前端可以直接访问
        String avatarUrl = "/api/files/resumes/user/avatar/" + fileName;
        
        log.info("文件实际存储路径: {}", filePath.toAbsolutePath());
        log.info("生成的访问URL: {}", avatarUrl);
        
        // 更新用户头像
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        
        log.info("用户头像上传成功，URL: {}", avatarUrl);
        return avatarUrl;
    }

    @Override
    public boolean changePassword(Long userId, PasswordChangeDTO passwordDTO) {
        log.info("修改用户ID={}的密码", userId);
        
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 检查旧密码是否正确
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        
        // 检查新密码
        if (passwordDTO.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        
        // 检查新密码与确认密码是否一致
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户密码修改成功");
        return true;
    }

    @Override
    public boolean bindPhone(Long userId, String phone, String code) {
        log.info("绑定用户ID={}的手机号: {}", userId, phone);
        
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 检查验证码（这里简化处理，实际应该调用验证码校验服务）
        if (!"1234".equals(code)) { // 假设1234是有效的验证码
            throw new IllegalArgumentException("验证码错误");
        }
        
        // 更新手机号
        user.setPhone(phone);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户手机号绑定成功");
        return true;
    }

    @Override
    public boolean bindEmail(Long userId, String email, String code) {
        log.info("绑定用户ID={}的邮箱: {}", userId, email);
        
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 检查邮箱是否被其他用户使用
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("该邮箱已被其他用户使用");
        }
        
        // 检查验证码（这里简化处理，实际应该调用验证码校验服务）
        if (!"1234".equals(code)) { // 假设1234是有效的验证码
            throw new IllegalArgumentException("验证码错误");
        }
        
        // 更新邮箱
        user.setEmail(email);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户邮箱绑定成功");
        return true;
    }
    
    /**
     * 将用户实体转换为DTO
     */
    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setAvatar(user.getAvatar());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        return dto;
    }
} 