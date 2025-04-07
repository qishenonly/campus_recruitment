package com.campus.controller.admin;

import com.campus.annotation.OperationLog;
import com.campus.dto.PageDTO;
import com.campus.dto.ResponseDTO;
import com.campus.dto.StudentDTO;
import com.campus.service.AdminStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 学生管理控制器
 */
@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    @Autowired
    private AdminStudentService adminStudentService;

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
    @GetMapping
    public ResponseDTO<PageDTO<StudentDTO>> getStudentList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "school", required = false) String school,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "status", required = false) String status) {
        return adminStudentService.getStudentList(pageNum, pageSize, name, school, major, status);
    }

    /**
     * 获取学生详情
     * @param id 学生ID
     * @return 学生详情
     */
    @GetMapping("/{id}")
    public ResponseDTO<StudentDTO> getStudentDetail(@PathVariable Long id) {
        return adminStudentService.getStudentDetail(id);
    }

    /**
     * 更新学生信息
     * @param id 学生ID
     * @param studentDTO 学生信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @OperationLog(operationType = "学生管理", description = "更新学生信息")
    public ResponseDTO<Void> updateStudent(
            @PathVariable Long id, 
            @RequestBody @Valid StudentDTO studentDTO) {
        return adminStudentService.updateStudent(id, studentDTO);
    }

    /**
     * 审核通过学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    @PostMapping("/{id}/verify")
    @OperationLog(operationType = "学生管理", description = "认证学生")
    public ResponseDTO<Void> verifyStudent(@PathVariable Long id) {
        return adminStudentService.verifyStudent(id);
    }

    /**
     * 禁用学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    @PostMapping("/{id}/disable")
    @OperationLog(operationType = "学生管理", description = "禁用学生账号")
    public ResponseDTO<Void> disableStudent(@PathVariable Long id) {
        return adminStudentService.disableStudent(id);
    }

    /**
     * 启用学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    @PostMapping("/{id}/enable")
    @OperationLog(operationType = "学生管理", description = "启用学生账号")
    public ResponseDTO<Void> enableStudent(@PathVariable Long id) {
        return adminStudentService.enableStudent(id);
    }

    /**
     * 删除学生账号
     * @param id 学生ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @OperationLog(operationType = "学生管理", description = "删除学生")
    public ResponseDTO<Void> deleteStudent(@PathVariable Long id) {
        return adminStudentService.deleteStudent(id);
    }

    /**
     * 重置学生密码
     * @param id 学生ID
     * @return 操作结果
     */
    @PostMapping("/{id}/reset-password")
    @OperationLog(operationType = "学生管理", description = "重置学生密码")
    public ResponseDTO<String> resetPassword(@PathVariable Long id) {
        return adminStudentService.resetPassword(id);
    }

    /**
     * 批量审核学生
     * @param ids 学生ID数组
     * @return 操作结果
     */
    @PostMapping("/batch-verify")
    public ResponseDTO<Void> batchVerifyStudents(@RequestBody List<Long> ids) {
        return adminStudentService.batchVerifyStudents(ids);
    }

    /**
     * 批量禁用学生账号
     * @param ids 学生ID数组
     * @return 操作结果
     */
    @PostMapping("/batch-disable")
    public ResponseDTO<Void> batchDisableStudents(@RequestBody List<Long> ids) {
        return adminStudentService.batchDisableStudents(ids);
    }

    /**
     * 批量启用学生账号
     * @param ids 学生ID数组
     * @return 操作结果
     */
    @PostMapping("/batch-enable")
    public ResponseDTO<Void> batchEnableStudents(@RequestBody List<Long> ids) {
        return adminStudentService.batchEnableStudents(ids);
    }

    /**
     * 批量删除学生账号 (DELETE方法)
     * @param ids 学生ID数组
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ResponseDTO<Void> batchDeleteStudents(@RequestBody List<Long> ids) {
        return adminStudentService.batchDeleteStudents(ids);
    }
    
    /**
     * 批量删除学生账号 (POST方法，适配前端)
     * @param ids 学生ID数组
     * @return 操作结果
     */
    @PostMapping("/batch-delete")
    public ResponseDTO<Void> batchDeleteStudentsPost(@RequestBody List<Long> ids) {
        return adminStudentService.batchDeleteStudents(ids);
    }

    /**
     * 导出学生数据
     * @param name 学生姓名
     * @param school 学校
     * @param major 专业
     * @param status 账号状态
     * @param response HTTP响应
     */
    @GetMapping("/export")
    public void exportStudents(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "school", required = false) String school,
            @RequestParam(value = "major", required = false) String major,
            @RequestParam(value = "status", required = false) String status,
            HttpServletResponse response) {
        adminStudentService.exportStudents(name, school, major, status, response);
    }

    /**
     * 导入学生数据
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseDTO<String> importStudents(@RequestParam("file") MultipartFile file) {
        return adminStudentService.importStudents(file);
    }

    /**
     * 下载学生导入模板
     * @param response HTTP响应
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        adminStudentService.downloadTemplate(response);
    }
} 