package com.campus.service.impl;

import com.campus.dto.AdminDTO;
import com.campus.dto.LoginDTO;
import com.campus.dto.PasswordDTO;
import com.campus.dto.ResponseDTO;
import com.campus.model.User;
import com.campus.repository.UserRepository;
import com.campus.service.AdminService;
import com.campus.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseDTO<String> login(LoginDTO loginDTO) {
        try {
            // 从users表中查找管理员账号
            Optional<User> userOpt = userRepository.findByUsername(loginDTO.getUsername());
            
            if (!userOpt.isPresent()) {
                return ResponseDTO.error("用户名或密码错误");
            }
            
            User user = userOpt.get();
            
            // 检查是否为管理员账号
            if (user.getRole() != User.UserRole.ADMIN) {
                return ResponseDTO.error("非管理员账号无法登录管理后台");
            }
            
            // 检查账号状态
            if (user.getStatus() == User.UserStatus.BLOCKED) {
                return ResponseDTO.error("账号已被锁定");
            }
            
            // 验证密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                return ResponseDTO.error("用户名或密码错误");
            }
            
            // 更新最后登录时间
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "ADMIN");
            
            return ResponseDTO.success(token);
        } catch (Exception e) {
            log.error("管理员登录失败", e);
            return ResponseDTO.error("登录失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<AdminDTO> getInfo(HttpServletRequest request) {
        try {
            Long adminId = jwtUtil.getUserIdFromRequest(request);
            if (adminId == null) {
                return ResponseDTO.error(401, "未获取到用户信息，请重新登录");
            }
            
            // 从users表中查找管理员
            Optional<User> userOpt = userRepository.findById(adminId);
            if (!userOpt.isPresent()) {
                return ResponseDTO.error(404, "管理员不存在");
            }
            
            User user = userOpt.get();
            if (user.getRole() != User.UserRole.ADMIN) {
                return ResponseDTO.error(403, "非管理员账号");
            }
            
            // 转换为AdminDTO
            AdminDTO adminDTO = new AdminDTO();
            adminDTO.setId(user.getId());
            adminDTO.setUsername(user.getUsername());
            adminDTO.setRealName(user.getRealName());
            adminDTO.setEmail(user.getEmail());
            adminDTO.setPhone(user.getPhone());
            adminDTO.setTwoFactorAuth(false); // 用户表没有此字段，默认为false
            
            return ResponseDTO.success(adminDTO);
        } catch (Exception e) {
            log.error("获取管理员信息失败", e);
            return ResponseDTO.error("获取管理员信息失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateInfo(AdminDTO adminDTO, HttpServletRequest request) {
        try {
            Long adminId = jwtUtil.getUserIdFromRequest(request);
            if (adminId == null) {
                return ResponseDTO.error(401, "未获取到用户信息，请重新登录");
            }
            
            // 从users表中查找管理员
            Optional<User> userOpt = userRepository.findById(adminId);
            if (!userOpt.isPresent()) {
                return ResponseDTO.error(404, "管理员不存在");
            }
            
            User user = userOpt.get();
            if (user.getRole() != User.UserRole.ADMIN) {
                return ResponseDTO.error(403, "非管理员账号");
            }
            
            // 更新基本信息
            user.setRealName(adminDTO.getRealName());
            user.setEmail(adminDTO.getEmail());
            user.setPhone(adminDTO.getPhone());
            user.setUpdateTime(LocalDateTime.now());
            
            userRepository.save(user);
            
            return ResponseDTO.success();
        } catch (Exception e) {
            log.error("更新管理员信息失败", e);
            return ResponseDTO.error("更新管理员信息失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> changePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        try {
            Long adminId = jwtUtil.getUserIdFromRequest(request);
            if (adminId == null) {
                return ResponseDTO.error(401, "未获取到用户信息，请重新登录");
            }
            
            // 从users表中查找管理员
            Optional<User> userOpt = userRepository.findById(adminId);
            if (!userOpt.isPresent()) {
                return ResponseDTO.error(404, "管理员不存在");
            }
            
            User user = userOpt.get();
            if (user.getRole() != User.UserRole.ADMIN) {
                return ResponseDTO.error(403, "非管理员账号");
            }
            
            // 验证旧密码
            if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
                return ResponseDTO.error("旧密码错误");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            
            return ResponseDTO.success();
        } catch (Exception e) {
            log.error("修改管理员密码失败", e);
            return ResponseDTO.error("修改管理员密码失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> logout(HttpServletRequest request) {
        // JWT是无状态的，客户端删除即可，但可以在此记录退出日志等操作
        return ResponseDTO.success();
    }
} 