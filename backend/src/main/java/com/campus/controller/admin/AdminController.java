package com.campus.controller.admin;

import com.campus.dto.AdminDTO;
import com.campus.dto.LoginDTO;
import com.campus.dto.PasswordDTO;
import com.campus.dto.ResponseDTO;
import com.campus.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseDTO<String> login(@RequestBody @Valid LoginDTO loginDTO) {
        return adminService.login(loginDTO);
    }

    /**
     * 获取管理员信息
     * @param request 请求
     * @return 管理员信息
     */
    @GetMapping("/info")
    public ResponseDTO<AdminDTO> getInfo(HttpServletRequest request) {
        return adminService.getInfo(request);
    }

    /**
     * 更新管理员信息
     * @param adminDTO 管理员信息
     * @param request 请求
     * @return 更新结果
     */
    @PutMapping("/info")
    public ResponseDTO<Void> updateInfo(@RequestBody @Valid AdminDTO adminDTO, HttpServletRequest request) {
        return adminService.updateInfo(adminDTO, request);
    }

    /**
     * 修改密码
     * @param passwordDTO 密码信息
     * @param request 请求
     * @return 修改结果
     */
    @PutMapping("/password")
    public ResponseDTO<Void> changePassword(@RequestBody @Valid PasswordDTO passwordDTO, HttpServletRequest request) {
        return adminService.changePassword(passwordDTO, request);
    }

    /**
     * 退出登录
     * @param request 请求
     * @return 退出结果
     */
    @PostMapping("/logout")
    public ResponseDTO<Void> logout(HttpServletRequest request) {
        return adminService.logout(request);
    }
} 