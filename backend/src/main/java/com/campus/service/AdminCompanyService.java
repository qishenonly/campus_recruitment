package com.campus.service;

import com.campus.dto.CompanyDTO;
import com.campus.dto.JobDTO;
import com.campus.dto.PageDTO;
import com.campus.dto.ResponseDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 企业管理服务接口
 */
public interface AdminCompanyService {
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
    ResponseDTO<PageDTO<CompanyDTO>> getCompanyList(Integer pageNum, Integer pageSize, String name, String industry, String scale, String status, String sortBy, String sortDir, String startTime, String endTime);

    /**
     * 获取企业详情
     * @param id 企业ID
     * @return 企业详情
     */
    ResponseDTO<CompanyDTO> getCompanyDetail(Long id);

    /**
     * 添加企业
     * @param companyDTO 企业信息
     * @return 添加结果
     */
    ResponseDTO<Long> addCompany(CompanyDTO companyDTO);

    /**
     * 更新企业信息
     * @param id 企业ID
     * @param companyDTO 企业信息
     * @return 更新结果
     */
    ResponseDTO<Void> updateCompany(Long id, CompanyDTO companyDTO);

    /**
     * 审核企业账号
     * @param id 企业ID
     * @param status 状态
     * @return 操作结果
     */
    ResponseDTO<Void> verifyCompany(Long id, String status);

    /**
     * 禁用企业账号
     * @param id 企业ID
     * @return 操作结果
     */
    ResponseDTO<Void> disableCompany(Long id);

    /**
     * 启用企业账号
     * @param id 企业ID
     * @return 操作结果
     */
    ResponseDTO<Void> enableCompany(Long id);

    /**
     * 删除企业账号
     * @param id 企业ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteCompany(Long id);

    /**
     * 获取企业发布的职位列表
     * @param id 企业ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 职位列表
     */
    ResponseDTO<PageDTO<JobDTO>> getCompanyJobs(Long id, Integer pageNum, Integer pageSize);

    /**
     * 批量审核企业
     * @param ids 企业ID数组
     * @param status 状态
     * @return 操作结果
     */
    ResponseDTO<Void> batchVerifyCompanies(List<Long> ids, String status);

    /**
     * 批量禁用企业账号
     * @param ids 企业ID数组
     * @return 操作结果
     */
    ResponseDTO<Void> batchDisableCompanies(List<Long> ids);

    /**
     * 批量启用企业账号
     * @param ids 企业ID数组
     * @return 操作结果
     */
    ResponseDTO<Void> batchEnableCompanies(List<Long> ids);

    /**
     * 批量删除企业账号
     * @param ids 企业ID数组
     * @return 操作结果
     */
    ResponseDTO<Void> batchDeleteCompanies(List<Long> ids);

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
    void exportCompanies(String name, String industry, String scale, String status, String sortBy, String sortDir, String startTime, String endTime, HttpServletResponse response);
    
    /**
     * 获取企业列表（兼容旧接口）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 企业名称
     * @param industry 行业
     * @param scale 规模
     * @param status 账号状态
     * @return 企业列表
     */
    default ResponseDTO<PageDTO<CompanyDTO>> getCompanyList(Integer pageNum, Integer pageSize, String name, String industry, String scale, String status) {
        return getCompanyList(pageNum, pageSize, name, industry, scale, status, "createTime", "desc", null, null);
    }
    
    /**
     * 审核通过企业账号（兼容旧接口）
     * @param id 企业ID
     * @return 操作结果
     */
    default ResponseDTO<Void> verifyCompany(Long id) {
        return verifyCompany(id, "ACTIVE");
    }
    
    /**
     * 批量审核企业（兼容旧接口）
     * @param ids 企业ID数组
     * @return 操作结果
     */
    default ResponseDTO<Void> batchVerifyCompanies(List<Long> ids) {
        return batchVerifyCompanies(ids, "ACTIVE");
    }
    
    /**
     * 导出企业数据（兼容旧接口）
     * @param name 企业名称
     * @param industry 行业
     * @param scale 规模
     * @param status 账号状态
     * @param response HTTP响应
     */
    default void exportCompanies(String name, String industry, String scale, String status, HttpServletResponse response) {
        exportCompanies(name, industry, scale, status, "createTime", "desc", null, null, response);
    }
} 