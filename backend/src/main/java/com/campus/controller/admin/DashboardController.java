package com.campus.controller.admin;

import com.campus.dto.ResponseDTO;
import com.campus.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * 获取基础统计数据
     * @return 基础统计数据
     */
    @GetMapping("/stats")
    public ResponseDTO<?> getBasicStats() {
        return adminDashboardService.getBasicStats();
    }

    /**
     * 获取用户注册趋势
     * @param type 统计类型：daily、weekly、monthly
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户注册趋势
     */
    @GetMapping("/register-trend")
    public ResponseDTO<?> getUserRegisterTrend(
            @RequestParam("type") String type,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        return adminDashboardService.getUserRegisterTrend(type, startDate, endDate);
    }

    /**
     * 获取职位统计
     * @return 职位统计
     */
    @GetMapping("/job-stats")
    public ResponseDTO<?> getJobStats() {
        return adminDashboardService.getJobStats();
    }

    /**
     * 获取简历投递统计
     * @param type 统计类型：daily、weekly、monthly
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 简历投递统计
     */
    @GetMapping("/resume-stats")
    public ResponseDTO<?> getResumeSubmitStats(
            @RequestParam("type") String type,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        return adminDashboardService.getResumeSubmitStats(type, startDate, endDate);
    }

    /**
     * 获取热门职位类别
     * @param limit 获取数量
     * @return 热门职位类别
     */
    @GetMapping("/hot-categories")
    public ResponseDTO<?> getHotJobCategories(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return adminDashboardService.getHotJobCategories(limit);
    }

    /**
     * 获取热门学校排名
     * @param limit 获取数量
     * @return 热门学校排名
     */
    @GetMapping("/top-schools")
    public ResponseDTO<?> getTopSchools(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return adminDashboardService.getTopSchools(limit);
    }

    /**
     * 获取热门企业排名
     * @param limit 获取数量
     * @return 热门企业排名
     */
    @GetMapping("/top-companies")
    public ResponseDTO<?> getTopCompanies(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return adminDashboardService.getTopCompanies(limit);
    }

    /**
     * 获取系统访问统计
     * @param type 统计类型：daily、weekly、monthly
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 系统访问统计
     */
    @GetMapping("/visit-stats")
    public ResponseDTO<?> getVisitStats(
            @RequestParam("type") String type,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        return adminDashboardService.getVisitStats(type, startDate, endDate);
    }

    /**
     * 获取最近操作日志
     * @param limit 获取数量
     * @return 最近操作日志
     */
    @GetMapping("/recent-logs")
    public ResponseDTO<?> getRecentLogs(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return adminDashboardService.getRecentLogs(limit);
    }
} 