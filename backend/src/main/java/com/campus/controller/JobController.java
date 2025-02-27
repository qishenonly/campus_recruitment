package com.campus.controller;

import com.campus.model.Job;
import com.campus.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    // 获取职位列表（分页）
    @GetMapping
    public ResponseEntity<Page<Job>> getAllJobs(Pageable pageable) {
        return ResponseEntity.ok(jobService.findAll(pageable));
    }

    // 获取单个职位详情
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        return jobService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 搜索职位
    @GetMapping("/search")
    public ResponseEntity<Page<Job>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) Job.PositionType positionType,
            @RequestParam(required = false) String salary,
            Pageable pageable) {
        return ResponseEntity.ok(jobService.search(keyword, location, education, positionType, salary, pageable));
    }

    // 发布新职位
    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.save(job));
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
    public ResponseEntity<Page<Job>> getCompanyJobs(
            @PathVariable Long companyId,
            Pageable pageable) {
        return ResponseEntity.ok(jobService.findByCompanyId(companyId, pageable));
    }
} 