package com.campus.service.impl;

import com.campus.dto.ResponseDTO;
import com.campus.model.Job;
import com.campus.model.JobApplication;
import com.campus.model.Student;
import com.campus.model.Company;
import com.campus.model.SystemLog;
import com.campus.repository.JobRepository;
import com.campus.repository.JobApplicationRepository;
import com.campus.repository.StudentRepository;
import com.campus.repository.CompanyRepository;
import com.campus.repository.SystemLogRepository;
import com.campus.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private SystemLogRepository systemLogRepository;

    @Override
    public ResponseDTO<?> getBasicStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 学生数量
        long studentCount = studentRepository.count();
        long studentLastMonth = studentRepository.countByCreateTimeBefore(LocalDateTime.now().minusMonths(1));
        double studentGrowth = studentLastMonth > 0 
                ? ((double) (studentCount - studentLastMonth) / studentLastMonth) * 100
                : 100;
        
        // 企业数量
        long companyCount = companyRepository.count();
        long companyLastMonth = companyRepository.countByCreateTimeBefore(LocalDateTime.now().minusMonths(1));
        double companyGrowth = companyLastMonth > 0 
                ? ((double) (companyCount - companyLastMonth) / companyLastMonth) * 100
                : 100;
        
        // 职位数量
        long jobCount = jobRepository.count();
        long jobLastMonth = jobRepository.countByPublishDateBefore(LocalDateTime.now().minusMonths(1));
        double jobGrowth = jobLastMonth > 0 
                ? ((double) (jobCount - jobLastMonth) / jobLastMonth) * 100
                : 100;
        
        // 投递数量
        long applicationCount = jobApplicationRepository.count();
        long applicationLastMonth = jobApplicationRepository.countByCreateTimeBefore(LocalDateTime.now().minusMonths(1));
        double applicationGrowth = applicationLastMonth > 0 
                ? ((double) (applicationCount - applicationLastMonth) / applicationLastMonth) * 100
                : 100;
        
        stats.put("studentCount", studentCount);
        stats.put("studentGrowth", Math.round(studentGrowth * 10) / 10.0);
        stats.put("companyCount", companyCount);
        stats.put("companyGrowth", Math.round(companyGrowth * 10) / 10.0);
        stats.put("jobCount", jobCount);
        stats.put("jobGrowth", Math.round(jobGrowth * 10) / 10.0);
        stats.put("applicationCount", applicationCount);
        stats.put("applicationGrowth", Math.round(applicationGrowth * 10) / 10.0);
        
        return ResponseDTO.success(stats);
    }

    @Override
    public ResponseDTO<?> getUserRegisterTrend(String type, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime start = parseStartDate(type, startDate);
        LocalDateTime end = parseEndDate(endDate);
        
        // 根据type确定时间间隔和格式
        String format;
        if ("daily".equals(type)) {
            format = "MM-dd";
        } else if ("weekly".equals(type)) {
            format = "yyyy-'W'w";
        } else if ("monthly".equals(type)) {
            format = "yyyy-MM";
        } else {
            format = "yyyy-MM-dd";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        
        // 获取时间范围内的日期列表
        List<String> dates = new ArrayList<>();
        List<Long> studentData = new ArrayList<>();
        List<Long> companyData = new ArrayList<>();
        
        // 按照类型生成日期数据
        if ("daily".equals(type)) {
            for (LocalDateTime date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String dateStr = date.format(formatter);
                dates.add(dateStr);
                
                LocalDateTime nextDate = date.plusDays(1);
                studentData.add(studentRepository.countByCreateTimeBetween(date, nextDate));
                companyData.add(companyRepository.countByCreateTimeBetween(date, nextDate));
            }
        } else if ("weekly".equals(type)) {
            for (LocalDateTime date = start; !date.isAfter(end); date = date.plusWeeks(1)) {
                String dateStr = date.format(formatter);
                dates.add(dateStr);
                
                LocalDateTime nextDate = date.plusWeeks(1);
                studentData.add(studentRepository.countByCreateTimeBetween(date, nextDate));
                companyData.add(companyRepository.countByCreateTimeBetween(date, nextDate));
            }
        } else if ("monthly".equals(type)) {
            for (LocalDateTime date = start; !date.isAfter(end); date = date.plusMonths(1)) {
                String dateStr = date.format(formatter);
                dates.add(dateStr);
                
                LocalDateTime nextDate = date.plusMonths(1);
                studentData.add(studentRepository.countByCreateTimeBetween(date, nextDate));
                companyData.add(companyRepository.countByCreateTimeBetween(date, nextDate));
            }
        }
        
        result.put("dates", dates);
        result.put("studentData", studentData);
        result.put("companyData", companyData);
        
        return ResponseDTO.success(result);
    }

    @Override
    public ResponseDTO<?> getJobStats() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取职位城市分布
        List<Map<String, Object>> cityDistribution = jobRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Job::getLocation,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .limit(10)
                .collect(Collectors.toList());
        
        // 获取职位行业分布
        List<Map<String, Object>> industryDistribution = jobRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        job -> {
                            // 这里假设Job中有对应的字段或者关联的公司有industry字段
                            // 实际情况可能需要关联查询公司信息
                            return "未分类"; // 实际中需要替换为真实数据
                        },
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .limit(10)
                .collect(Collectors.toList());
        
        result.put("cityDistribution", cityDistribution);
        result.put("industryDistribution", industryDistribution);
        
        return ResponseDTO.success(result);
    }

    @Override
    public ResponseDTO<?> getResumeSubmitStats(String type, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime start = parseStartDate(type, startDate);
        LocalDateTime end = parseEndDate(endDate);
        
        // 根据type确定时间间隔和格式
        String format;
        if ("daily".equals(type)) {
            format = "MM-dd";
        } else if ("weekly".equals(type)) {
            format = "yyyy-'W'w";
        } else if ("monthly".equals(type)) {
            format = "yyyy-MM";
        } else {
            format = "yyyy-MM-dd";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        
        // 获取时间范围内的日期列表
        List<String> dates = new ArrayList<>();
        List<Long> submitData = new ArrayList<>();
        
        // 按照类型生成日期数据
        if ("daily".equals(type)) {
            for (LocalDateTime date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String dateStr = date.format(formatter);
                dates.add(dateStr);
                
                LocalDateTime nextDate = date.plusDays(1);
                submitData.add(jobApplicationRepository.countByCreateTimeBetween(date, nextDate));
            }
        } else if ("weekly".equals(type)) {
            for (LocalDateTime date = start; !date.isAfter(end); date = date.plusWeeks(1)) {
                String dateStr = date.format(formatter);
                dates.add(dateStr);
                
                LocalDateTime nextDate = date.plusWeeks(1);
                submitData.add(jobApplicationRepository.countByCreateTimeBetween(date, nextDate));
            }
        } else if ("monthly".equals(type)) {
            for (LocalDateTime date = start; !date.isAfter(end); date = date.plusMonths(1)) {
                String dateStr = date.format(formatter);
                dates.add(dateStr);
                
                LocalDateTime nextDate = date.plusMonths(1);
                submitData.add(jobApplicationRepository.countByCreateTimeBetween(date, nextDate));
            }
        }
        
        result.put("dates", dates);
        result.put("submitData", submitData);
        
        return ResponseDTO.success(result);
    }

    @Override
    public ResponseDTO<?> getHotJobCategories(Integer limit) {
        // 这里假设Job有category字段或关联关系
        // 若与实际不符，需要调整实现方式
        
        List<Map<String, Object>> hotCategories = jobRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Job::getPositionType,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey().toString());
                    item.put("value", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .limit(limit)
                .collect(Collectors.toList());
        
        return ResponseDTO.success(hotCategories);
    }

    @Override
    public ResponseDTO<?> getTopSchools(Integer limit) {
        // 获取学校排名（根据学生人数）
        List<Map<String, Object>> topSchools = studentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Student::getUniversity,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .limit(limit)
                .collect(Collectors.toList());
        
        return ResponseDTO.success(topSchools);
    }

    @Override
    public ResponseDTO<?> getTopCompanies(Integer limit) {
        // 获取企业排名（根据招聘职位数）
        List<Map<String, Object>> topCompanies = jobRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Job::getCompanyId,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    // 获取公司名称
                    Company company = companyRepository.findById(entry.getKey()).orElse(null);
                    item.put("name", company != null ? company.getCompanyName() : "未知企业");
                    item.put("value", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .limit(limit)
                .collect(Collectors.toList());
        
        return ResponseDTO.success(topCompanies);
    }

    @Override
    public ResponseDTO<?> getVisitStats(String type, String startDate, String endDate) {
        // 假设有系统访问日志表或相关统计
        // 这里模拟实现访问统计
        
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> visitData = new ArrayList<>();
        
        // 模拟数据
        if ("daily".equals(type)) {
            for (int i = 0; i < 7; i++) {
                dates.add(LocalDate.now().minusDays(6-i).format(DateTimeFormatter.ofPattern("MM-dd")));
                visitData.add(100 + (int)(Math.random() * 900));
            }
        } else if ("weekly".equals(type)) {
            for (int i = 0; i < 4; i++) {
                dates.add("第" + (i+1) + "周");
                visitData.add(1000 + (int)(Math.random() * 2000));
            }
        } else if ("monthly".equals(type)) {
            for (int i = 0; i < 12; i++) {
                dates.add(String.format("%02d月", i+1));
                visitData.add(3000 + (int)(Math.random() * 5000));
            }
        }
        
        result.put("dates", dates);
        result.put("visitData", visitData);
        
        return ResponseDTO.success(result);
    }

    @Override
    public ResponseDTO<?> getRecentLogs(Integer limit) {
        // 获取最近的系统日志
        List<SystemLog> logs = systemLogRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createTime"))).getContent();
        
        List<Map<String, Object>> recentLogs = logs.stream().map(log -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("time", log.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            item.put("type", log.getOperationType());
            item.put("content", log.getContent());
            item.put("user", log.getOperatorName());
            item.put("ip", log.getIpAddress());
            return item;
        }).collect(Collectors.toList());
        
        return ResponseDTO.success(recentLogs);
    }
    
    // 辅助方法：解析开始日期
    private LocalDateTime parseStartDate(String type, String startDate) {
        if (startDate != null && !startDate.isEmpty()) {
            return LocalDate.parse(startDate).atStartOfDay();
        }
        
        // 默认日期范围
        if ("daily".equals(type)) {
            return LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0);
        } else if ("weekly".equals(type)) {
            return LocalDateTime.now().minusWeeks(3).withHour(0).withMinute(0).withSecond(0);
        } else if ("monthly".equals(type)) {
            return LocalDateTime.now().minusMonths(11).withHour(0).withMinute(0).withSecond(0);
        } else {
            return LocalDateTime.now().minusDays(29).withHour(0).withMinute(0).withSecond(0);
        }
    }
    
    // 辅助方法：解析结束日期
    private LocalDateTime parseEndDate(String endDate) {
        if (endDate != null && !endDate.isEmpty()) {
            return LocalDate.parse(endDate).atTime(23, 59, 59);
        }
        return LocalDateTime.now();
    }
} 