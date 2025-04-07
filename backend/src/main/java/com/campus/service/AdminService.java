package com.campus.service;

import com.campus.dto.AdminDTO;
import com.campus.dto.LoginDTO;
import com.campus.dto.PasswordDTO;
import com.campus.dto.ResponseDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员服务接口
 */
public interface AdminService {
    /**
     * 管理员登录
     * @param loginDTO 登录信息
     * @return 登录结果（token）
     */
    ResponseDTO<String> login(LoginDTO loginDTO);

    /**
     * 获取管理员信息
     * @param request 请求
     * @return 管理员信息
     */
    ResponseDTO<AdminDTO> getInfo(HttpServletRequest request);

    /**
     * 更新管理员信息
     * @param adminDTO 管理员信息
     * @param request 请求
     * @return 更新结果
     */
    ResponseDTO<Void> updateInfo(AdminDTO adminDTO, HttpServletRequest request);

    /**
     * 修改密码
     * @param passwordDTO 密码信息
     * @param request 请求
     * @return 修改结果
     */
    ResponseDTO<Void> changePassword(PasswordDTO passwordDTO, HttpServletRequest request);

    /**
     * 退出登录
     * @param request 请求
     * @return 退出结果
     */
    ResponseDTO<Void> logout(HttpServletRequest request);
} 