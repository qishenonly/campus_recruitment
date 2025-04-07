package com.campus.controller.admin;

import com.campus.annotation.OperationLog;
import com.campus.dto.CompanyDTO;
import com.campus.dto.JobDTO;
import com.campus.dto.PageDTO;
import com.campus.dto.ResponseDTO;
import com.campus.service.AdminCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 企业管理控制器
 */
@RestController
@RequestMapping("/api/admin/companies")
public class AdminCompanyController {

    @Autowired
    private AdminCompanyService adminCompanyService;

    /**
     * 获取企业列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 企业名称
     * @param industry 行业
     * @param scale 规模
     * @param status 账号状态
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 企业列表
     */
    @GetMapping
    public ResponseDTO<PageDTO<CompanyDTO>> getCompanyList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "scale", required = false) String scale,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sortBy", defaultValue = "companyName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return adminCompanyService.getCompanyList(pageNum, pageSize, name, industry, scale, status, sortBy, sortDir, startTime, endTime);
    }

    /**
     * 获取企业详情
     * @param id 企业ID
     * @return 企业详情
     */
    @GetMapping("/{id}")
    public ResponseDTO<CompanyDTO> getCompanyDetail(@PathVariable Long id) {
        return adminCompanyService.getCompanyDetail(id);
    }

    /**
     * 添加企业
     * @param companyDTO 企业信息
     * @return 添加结果
     */
    @PostMapping
    @OperationLog(operationType = "企业管理", description = "添加企业")
    public ResponseDTO<Long> addCompany(@RequestBody @Valid CompanyDTO companyDTO) {
        return adminCompanyService.addCompany(companyDTO);
    }

    /**
     * 更新企业信息
     * @param id 企业ID
     * @param companyDTO 企业信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @OperationLog(operationType = "企业管理", description = "更新企业信息")
    public ResponseDTO<Void> updateCompany(
            @PathVariable Long id, 
            @RequestBody @Valid CompanyDTO companyDTO) {
        return adminCompanyService.updateCompany(id, companyDTO);
    }

    /**
     * 审核企业账号
     * @param id 企业ID
     * @param requestBody 请求体
     * @return 操作结果
     */
    @PostMapping("/{id}/verify")
    @OperationLog(operationType = "企业管理", description = "认证企业")
    public ResponseDTO<Void> verifyCompany(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        String status = requestBody.get("status");
        return adminCompanyService.verifyCompany(id, status);
    }

    /**
     * 禁用企业账号
     * @param id 企业ID
     * @return 操作结果
     */
    @PostMapping("/{id}/disable")
    @OperationLog(operationType = "企业管理", description = "禁用企业")
    public ResponseDTO<Void> disableCompany(@PathVariable Long id) {
        return adminCompanyService.disableCompany(id);
    }

    /**
     * 启用企业账号
     * @param id 企业ID
     * @return 操作结果
     */
    @PostMapping("/{id}/enable")
    @OperationLog(operationType = "企业管理", description = "启用企业")
    public ResponseDTO<Void> enableCompany(@PathVariable Long id) {
        return adminCompanyService.enableCompany(id);
    }

    /**
     * 删除企业账号
     * @param id 企业ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @OperationLog(operationType = "企业管理", description = "删除企业")
    public ResponseDTO<Void> deleteCompany(@PathVariable Long id) {
        return adminCompanyService.deleteCompany(id);
    }

    /**
     * 获取企业发布的职位列表
     * @param id 企业ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 职位列表
     */
    @GetMapping("/{id}/jobs")
    public ResponseDTO<PageDTO<JobDTO>> getCompanyJobs(
            @PathVariable Long id,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return adminCompanyService.getCompanyJobs(id, pageNum, pageSize);
    }

    /**
     * 批量审核企业
     * @param requestBody 包含企业ID数组和状态的请求体
     * @return 操作结果
     */
    @PostMapping("/batch/verify")
    public ResponseDTO<Void> batchVerifyCompanies(@RequestBody Map<String, Object> requestBody) {
        List<Long> ids = (List<Long>) requestBody.get("ids");
        String status = (String) requestBody.get("status");
        return adminCompanyService.batchVerifyCompanies(ids, status);
    }

    /**
     * 批量禁用企业账号
     * @param requestBody 包含企业ID数组的请求体
     * @return 操作结果
     */
    @PostMapping("/batch/disable")
    public ResponseDTO<Void> batchDisableCompanies(@RequestBody Map<String, List<Long>> requestBody) {
        List<Long> ids = requestBody.get("ids");
        return adminCompanyService.batchDisableCompanies(ids);
    }

    /**
     * 批量启用企业账号
     * @param requestBody 包含企业ID数组的请求体
     * @return 操作结果
     */
    @PostMapping("/batch/enable")
    public ResponseDTO<Void> batchEnableCompanies(@RequestBody Map<String, List<Long>> requestBody) {
        List<Long> ids = requestBody.get("ids");
        return adminCompanyService.batchEnableCompanies(ids);
    }

    /**
     * 批量删除企业账号
     * @param requestBody 包含企业ID数组的请求体
     * @return 操作结果
     */
    @PostMapping("/batch/delete")
    public ResponseDTO<Void> batchDeleteCompaniesPost(@RequestBody Map<String, List<Long>> requestBody) {
        List<Long> ids = requestBody.get("ids");
        return adminCompanyService.batchDeleteCompanies(ids);
    }

    /**
     * 导出企业数据
     * @param name 企业名称
     * @param industry 行业
     * @param scale 规模
     * @param status 账号状态
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param response HTTP响应
     */
    @GetMapping("/export")
    public void exportCompanies(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "scale", required = false) String scale,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sortBy", defaultValue = "companyName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            HttpServletResponse response) {
        adminCompanyService.exportCompanies(name, industry, scale, status, sortBy, sortDir, startTime, endTime, response);
    }
} 