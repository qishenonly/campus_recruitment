package com.campus.service;

import com.campus.dto.ResponseDTO;

/**
 * 仪表盘服务接口
 */
public interface AdminDashboardService {
    /**
     * 获取基础统计数据
     * @return 基础统计数据
     */
    ResponseDTO<?> getBasicStats();

    /**
     * 获取用户注册趋势
     * @param type 统计类型：daily、weekly、monthly
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户注册趋势
     */
    ResponseDTO<?> getUserRegisterTrend(String type, String startDate, String endDate);

    /**
     * 获取职位统计
     * @return 职位统计
     */
    ResponseDTO<?> getJobStats();

    /**
     * 获取简历投递统计
     * @param type 统计类型：daily、weekly、monthly
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 简历投递统计
     */
    ResponseDTO<?> getResumeSubmitStats(String type, String startDate, String endDate);

    /**
     * 获取热门职位类别
     * @param limit 获取数量
     * @return 热门职位类别
     */
    ResponseDTO<?> getHotJobCategories(Integer limit);

    /**
     * 获取热门学校排名
     * @param limit 获取数量
     * @return 热门学校排名
     */
    ResponseDTO<?> getTopSchools(Integer limit);

    /**
     * 获取热门企业排名
     * @param limit 获取数量
     * @return 热门企业排名
     */
    ResponseDTO<?> getTopCompanies(Integer limit);

    /**
     * 获取系统访问统计
     * @param type 统计类型：daily、weekly、monthly
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 系统访问统计
     */
    ResponseDTO<?> getVisitStats(String type, String startDate, String endDate);

    /**
     * 获取最近操作日志
     * @param limit 获取数量
     * @return 最近操作日志
     */
    ResponseDTO<?> getRecentLogs(Integer limit);
} 