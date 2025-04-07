package com.campus.service.impl;

import com.campus.dto.PasswordChangeDTO;
import com.campus.dto.StudentSettingsDTO;
import com.campus.model.Student;
import com.campus.model.User;
import com.campus.repository.StudentRepository;
import com.campus.repository.UserRepository;
import com.campus.service.StudentSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentSettingsServiceImpl implements StudentSettingsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public StudentSettingsDTO getStudentProfile(Long userId) {
        log.info("获取学生ID={}的个人信息", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 使用学生ID查询学生信息
        Optional<Student> studentOpt = studentRepository.findById(userId);
        
        StudentSettingsDTO dto = new StudentSettingsDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setAvatar(user.getAvatar());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        
        // 如果学生信息存在，填充学生特有字段
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            dto.setUniversity(student.getUniversity());
            dto.setMajor(student.getMajor());
            dto.setEducation(student.getEducation() != null ? student.getEducation().name() : null);
            dto.setGraduationYear(student.getGraduationYear());
            dto.setLocation(student.getLocation());
            dto.setIntroducation(student.getExpectedPosition());
        }
        
        return dto;
    }

    @Override
    @Transactional
    public StudentSettingsDTO updateStudentProfile(Long userId, StudentSettingsDTO profileDTO) {
        log.info("更新学生ID={}的个人信息", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        // 更新用户基本信息
        user.setRealName(profileDTO.getRealName());
        
        // 如果修改了用户名，检查是否唯一
        if (!user.getUsername().equals(profileDTO.getUsername())) {
            if (userRepository.existsByUsername(profileDTO.getUsername())) {
                throw new IllegalArgumentException("该用户名已被使用");
            }
            user.setUsername(profileDTO.getUsername());
        }
        
        // 如果修改了邮箱，检查是否唯一
        if (profileDTO.getEmail() != null && !profileDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(profileDTO.getEmail())) {
                throw new IllegalArgumentException("该邮箱已被其他用户使用");
            }
            user.setEmail(profileDTO.getEmail());
        }
        
        // 更新手机号
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        
        // 更新时间
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        
        // 获取或创建学生信息
        Student student = studentRepository.findById(userId)
                .orElseGet(() -> {
                    Student newStudent = new Student();
                    newStudent.setId(userId);  // 使用用户ID作为学生ID
                    return newStudent;
                });
        
        // 更新学生特有信息
        student.setUniversity(profileDTO.getUniversity());
        student.setMajor(profileDTO.getMajor());
        if (profileDTO.getEducation() != null) {
            try {
                student.setEducation(Student.Education.valueOf(profileDTO.getEducation()));
            } catch (IllegalArgumentException e) {
                log.warn("无效的学历值: {}", profileDTO.getEducation());
            }
        }
        student.setGraduationYear(profileDTO.getGraduationYear());
        student.setLocation(profileDTO.getLocation());
        student.setExpectedPosition(profileDTO.getIntroducation());
        
        // 保存学生信息
        studentRepository.save(student);
        
        // 返回更新后的信息
        return getStudentProfile(userId);
    }

    @Override
    public String uploadStudentAvatar(Long userId, MultipartFile file) throws IOException {
        log.info("上传学生ID={}的头像", userId);
        
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
        
        // 构建访问URL
        String avatarUrl = accessPath + "/user/avatar/" + fileName;
        
        // 更新用户头像
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        
        log.info("学生头像上传成功，URL: {}", avatarUrl);
        return avatarUrl;
    }

    @Override
    public boolean changePassword(Long userId, PasswordChangeDTO passwordDTO) {
        log.info("修改学生ID={}的密码", userId);
        
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
        
        log.info("学生密码修改成功");
        return true;
    }

    @Override
    public boolean bindPhone(Long userId, String phone, String code) {
        log.info("绑定学生ID={}的手机号: {}", userId, phone);
        
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
        
        log.info("学生手机号绑定成功");
        return true;
    }

    @Override
    public boolean bindEmail(Long userId, String email, String code) {
        log.info("绑定学生ID={}的邮箱: {}", userId, email);
        
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
        
        log.info("学生邮箱绑定成功");
        return true;
    }

    @Override
    public boolean sendPhoneCode(String phone) {
        log.info("发送验证码到手机: {}", phone);
        
        // 这里应该调用短信服务发送验证码
        // 简化处理，直接返回成功
        
        log.info("验证码发送成功");
        return true;
    }

    @Override
    public boolean sendEmailCode(String email) {
        log.info("发送验证码到邮箱: {}", email);
        
        // 这里应该调用邮件服务发送验证码
        // 简化处理，直接返回成功
        
        log.info("验证码发送成功");
        return true;
    }
} 