package com.campus.controller;

import com.campus.model.Job;
import com.campus.service.JobService;
import com.campus.dto.JobDTO;
import com.campus.dto.CompanyInfoDTO;
import com.campus.util.JwtUtil;
import com.campus.service.CompanyService;
import com.campus.service.CompanyInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;
    
    @Autowired
    private CompanyInfoService companyInfoService;

    // 获取职位列表（分页）
    @GetMapping
    public ResponseEntity<Page<JobDTO>> getAllJobs(Pageable pageable) {
        return ResponseEntity.ok(jobService.findAllWithCompany(pageable));
    }

    // 获取单个职位详情
    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJob(@PathVariable Long id) {
        return jobService.findByIdWithCompany(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 根据职位ID获取公司信息
    @GetMapping("/{id}/company")
    public ResponseEntity<?> getCompanyByJobId(@PathVariable Long id) {
        try {
            // 获取职位所属的公司ID
            Long companyId = jobService.getCompanyIdByJobId(id);
            
            // 获取公司详细信息
            CompanyInfoDTO companyInfo = companyInfoService.getCompanyInfo(companyId);
            
            // 获取职位详细信息
            JobDTO jobInfo = jobService.findByIdWithCompany(id)
                    .orElseThrow(() -> new RuntimeException("职位不存在"));
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("company", companyInfo);
            responseData.put("job", jobInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取职位所属公司信息成功");
            response.put("data", responseData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取职位所属公司信息失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 搜索职位
    @GetMapping("/search")
    public ResponseEntity<Page<JobDTO>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) Job.PositionType positionType,
            @RequestParam(required = false) String salary,
            Pageable pageable) {
        return ResponseEntity.ok(jobService.searchWithCompany(
            keyword, location, education, positionType, salary, pageable));
    }

    // 更新职位信息
    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        job.setId(id);
        return ResponseEntity.ok(jobService.save(job));
    }

    // 更改职位状态
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateJobStatus(
            @PathVariable Long id,
            @RequestParam Job.JobStatus status) {
        jobService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    // 增加浏览次数
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        jobService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    // 获取企业发布的职位列表
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<JobDTO>> getCompanyJobs(
            @PathVariable Long companyId,
            Pageable pageable) {
        return ResponseEntity.ok(jobService.findByCompanyIdWithCompany(companyId, pageable));
    }
} 