package com.campus.service;

import com.campus.dto.PageDTO;
import com.campus.dto.ResponseDTO;
import com.campus.dto.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 学生管理服务接口
 */
public interface AdminStudentService {
    /**
     * 获取学生列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 学生姓名
     * @param school 学校
     * @param major 专业
     * @param status 账号状态
     * @return 学生列表
     */
    ResponseDTO<PageDTO<StudentDTO>> getStudentList(Integer pageNum, Integer pageSize, String name, String school, String major, String status);

    /**
     * 获取学生详情
     * @param id 学生ID
     * @return 学生详情
     */
    ResponseDTO<StudentDTO> getStudentDetail(Long id);

    /**
     * 更新学生信息
     * @param id 学生ID
     * @param studentDTO 学生信息
     * @return 更新结果
     */
    ResponseDTO<Void> updateStudent(Long id, StudentDTO studentDTO);

    /**
     * 审核通过学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    ResponseDTO<Void> verifyStudent(Long id);

    /**
     * 禁用学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    ResponseDTO<Void> disableStudent(Long id);

    /**
     * 启用学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    ResponseDTO<Void> enableStudent(Long id);

    /**
     * 删除学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteStudent(Long id);

    /**
     * 重置学生密码
     * @param id 学生ID
     * @return 操作结果
     */
    ResponseDTO<String> resetPassword(Long id);

    /**
     * 批量审核学生
     * @param ids 学生ID数组
     * @return 操作结果
     */
    ResponseDTO<Void> batchVerifyStudents(List<Long> ids);

    /**
     * 批量禁用学生账号
     * @param ids 学生ID数组
     * @return 操作结果
     */
    ResponseDTO<Void> batchDisableStudents(List<Long> ids);

    /**
     * 批量启用学生账号
     * @param ids 学生ID数组
     * @return 操作结果
     */
    ResponseDTO<Void> batchEnableStudents(List<Long> ids);

    /**
     * 批量删除学生账号
     * @param ids 学生ID数组
     * @return 操作结果
     */
    ResponseDTO<Void> batchDeleteStudents(List<Long> ids);

    /**
     * 导出学生数据
     * @param name 学生姓名
     * @param school 学校
     * @param major 专业
     * @param status 账号状态
     * @param response HTTP响应
     */
    void exportStudents(String name, String school, String major, String status, HttpServletResponse response);

    /**
     * 导入学生数据
     * @param file Excel文件
     * @return 导入结果
     */
    ResponseDTO<String> importStudents(MultipartFile file);

    /**
     * 下载学生导入模板
     * @param response HTTP响应
     */
    void downloadTemplate(HttpServletResponse response);
} 