package com.campus.service.impl;

import com.campus.dto.AdminDTO;
import com.campus.dto.LoginDTO;
import com.campus.dto.PasswordDTO;
import com.campus.dto.ResponseDTO;
import com.campus.model.Admin;
import com.campus.repository.AdminRepository;
import com.campus.service.AdminService;
import com.campus.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseDTO<String> login(LoginDTO loginDTO) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(loginDTO.getUsername());
        
        if (!adminOpt.isPresent()) {
            return ResponseDTO.error("用户名或密码错误");
        }
        
        Admin admin = adminOpt.get();
        
        // 检查账号状态
        if (admin.getStatus() == Admin.AdminStatus.LOCKED) {
            return ResponseDTO.error("账号已被锁定");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
            return ResponseDTO.error("用户名或密码错误");
        }
        
        // 更新最后登录时间
        admin.setLastLoginTime(LocalDateTime.now());
        adminRepository.save(admin);
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "ADMIN");
        
        return ResponseDTO.success(token);
    }

    @Override
    public ResponseDTO<AdminDTO> getInfo(HttpServletRequest request) {
        try {
            Long adminId = jwtUtil.getUserIdFromRequest(request);
            if (adminId == null) {
                return ResponseDTO.error(401, "未获取到用户信息，请重新登录");
            }
            
            Optional<Admin> adminOpt = adminRepository.findById(adminId);
            if (!adminOpt.isPresent()) {
                return ResponseDTO.error(404, "管理员不存在");
            }
            
            Admin admin = adminOpt.get();
            AdminDTO adminDTO = new AdminDTO();
            BeanUtils.copyProperties(admin, adminDTO);
            
            return ResponseDTO.success(adminDTO);
        } catch (Exception e) {
            // 记录错误日志
            e.printStackTrace();
            return ResponseDTO.error("获取管理员信息失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateInfo(AdminDTO adminDTO, HttpServletRequest request) {
        Long adminId = jwtUtil.getUserIdFromRequest(request);
        
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 更新基本信息
        admin.setRealName(adminDTO.getRealName());
        admin.setEmail(adminDTO.getEmail());
        admin.setPhone(adminDTO.getPhone());
        admin.setTwoFactorAuth(adminDTO.getTwoFactorAuth());
        
        adminRepository.save(admin);
        
        return ResponseDTO.success();
    }

    @Override
    public ResponseDTO<Void> changePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        Long adminId = jwtUtil.getUserIdFromRequest(request);
        
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        // 验证旧密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), admin.getPassword())) {
            return ResponseDTO.error("旧密码错误");
        }
        
        // 更新密码
        admin.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        adminRepository.save(admin);
        
        return ResponseDTO.success();
    }

    @Override
    public ResponseDTO<Void> logout(HttpServletRequest request) {
        // JWT无状态，客户端需要删除token
        return ResponseDTO.success();
    }
} 