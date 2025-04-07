package com.campus.service.impl;

import com.campus.dto.CompanyDTO;
import com.campus.dto.JobDTO;
import com.campus.dto.PageDTO;
import com.campus.dto.ResponseDTO;
import com.campus.model.Company;
import com.campus.model.User;
import com.campus.repository.CompanyRepository;
import com.campus.repository.JobRepository;
import com.campus.repository.UserRepository;
import com.campus.service.AdminCompanyService;
import com.campus.service.JobService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCompanyServiceImpl implements AdminCompanyService {

    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private JobService jobService;

    @Override
    public ResponseDTO<PageDTO<CompanyDTO>> getCompanyList(Integer pageNum, Integer pageSize, String name, String industry, String scale, String status, String sortBy, String sortDir, String startTime, String endTime) {
        // 创建排序对象
        // 确保使用正确的排序字段，防止使用不存在的字段
        String sortField = "companyName"; // 默认排序字段
        
        // 根据传入的排序字段映射到实际的实体字段
        if (sortBy != null) {
            switch (sortBy) {
                case "name":
                case "companyName":
                    sortField = "companyName";
                    break;
                case "industry":
                    sortField = "industry";
                    break;
                case "scale":
                    sortField = "scale";
                    break;
                case "city":
                case "location":
                    sortField = "city";
                    break;
                // 其他可能的排序字段...
                default:
                    sortField = "companyName";
                    break;
            }
        }
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(Sort.Direction.ASC, sortField) 
            : Sort.by(Sort.Direction.DESC, sortField);
        
        // 创建分页请求
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
        
        // 获取企业列表
        Page<Company> companyPage;
        
        // 根据条件查询
        if (name != null && !name.isEmpty()) {
            companyPage = companyRepository.findByCompanyNameContainingOrIndustryContaining(name, name, pageable);
        } else {
            companyPage = companyRepository.findAll(pageable);
        }
        
        // 解析日期时间范围
        LocalDateTime start = null;
        LocalDateTime end = null;
        
        if (startTime != null && !startTime.isEmpty()) {
            try {
                start = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // 日期格式解析错误
                return ResponseDTO.error("开始时间格式错误");
            }
        }
        
        if (endTime != null && !endTime.isEmpty()) {
            try {
                end = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // 日期格式解析错误
                return ResponseDTO.error("结束时间格式错误");
            }
        }
        
        // 最终日期范围
        final LocalDateTime finalStart = start;
        final LocalDateTime finalEnd = end;
        
        // 转换为DTO
        List<CompanyDTO> companyDTOs = companyPage.getContent().stream()
                .filter(company -> {
                    // 根据条件过滤
                    boolean match = true;
                    
                    if (industry != null && !industry.isEmpty()) {
                        match = match && company.getIndustry() != null && company.getIndustry().contains(industry);
                    }
                    
                    if (scale != null && !scale.isEmpty()) {
                        match = match && company.getScale() != null && company.getScale().equals(scale);
                    }
                    
                    if (status != null && !status.isEmpty()) {
                        match = match && company.getUser() != null && 
                                company.getUser().getStatus().name().equals(status);
                    }
                    
                    // 日期时间筛选
                    if (finalStart != null) {
                        match = match && company.getUser() != null && 
                                company.getUser().getCreateTime() != null &&
                                company.getUser().getCreateTime().isAfter(finalStart);
                    }
                    
                    if (finalEnd != null) {
                        match = match && company.getUser() != null && 
                                company.getUser().getCreateTime() != null &&
                                company.getUser().getCreateTime().isBefore(finalEnd);
                    }
                    
                    return match;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 创建分页DTO
        PageDTO<CompanyDTO> pageDTO = new PageDTO<>();
        pageDTO.setList(companyDTOs);
        pageDTO.setTotal(companyPage.getTotalElements());
        pageDTO.setPageNum(pageNum);
        pageDTO.setPageSize(pageSize);
        pageDTO.setPages(companyPage.getTotalPages());
        
        return ResponseDTO.success(pageDTO);
    }

    @Override
    public ResponseDTO<CompanyDTO> getCompanyDetail(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        return ResponseDTO.success(convertToDTO(company));
    }

    @Override
    @Transactional
    public ResponseDTO<Long> addCompany(CompanyDTO companyDTO) {
        // 创建用户
        User user = new User();
        user.setUsername(companyDTO.getEmail());
        user.setEmail(companyDTO.getEmail());
        user.setPhone(companyDTO.getPhone());
        user.setRealName(companyDTO.getName());
        user.setPassword("123456"); // 初始密码
        user.setRole(User.UserRole.COMPANY);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // 创建企业
        Company company = new Company();
        company.setId(savedUser.getId());
        company.setCompanyName(companyDTO.getName());
        company.setIndustry(companyDTO.getIndustry());
        company.setScale(companyDTO.getScale());
        company.setCity(companyDTO.getLocation());
        company.setContactPerson(companyDTO.getContactPerson());
        company.setContactPosition("HR"); // 默认职位
        company.setUser(savedUser);
        company.setVerified(false);
        
        Company savedCompany = companyRepository.save(company);
        
        return ResponseDTO.success(savedCompany.getId());
    }

    @Override
    @Transactional
    public ResponseDTO<Void> updateCompany(Long id, CompanyDTO companyDTO) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        User user = company.getUser();
        
        // 更新用户信息
        if (companyDTO.getEmail() != null) {
            user.setEmail(companyDTO.getEmail());
        }
        
        if (companyDTO.getPhone() != null) {
            user.setPhone(companyDTO.getPhone());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        
        // 更新企业信息
        if (companyDTO.getName() != null) {
            company.setCompanyName(companyDTO.getName());
        }
        
        if (companyDTO.getIndustry() != null) {
            company.setIndustry(companyDTO.getIndustry());
        }
        
        if (companyDTO.getScale() != null) {
            company.setScale(companyDTO.getScale());
        }
        
        if (companyDTO.getLocation() != null) {
            company.setCity(companyDTO.getLocation());
        }
        
        if (companyDTO.getContactPerson() != null) {
            company.setContactPerson(companyDTO.getContactPerson());
        }
        
        companyRepository.save(company);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> verifyCompany(Long id, String status) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        if (company.getUser() == null) {
            return ResponseDTO.error("企业账号不存在");
        }
        
        if (status.equals("ACTIVE")) {
            company.setVerified(true);
            company.getUser().setStatus(User.UserStatus.ACTIVE);
        } else if (status.equals("INACTIVE")) {
            company.setVerified(false);
            company.getUser().setStatus(User.UserStatus.INACTIVE);
        } else if (status.equals("BLOCKED")) {
            company.getUser().setStatus(User.UserStatus.BLOCKED);
        } else {
            return ResponseDTO.error("无效的状态");
        }
        
        companyRepository.save(company);
        userRepository.save(company.getUser());
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> disableCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        User user = company.getUser();
        user.setStatus(User.UserStatus.BLOCKED);
        userRepository.save(user);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> enableCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        User user = company.getUser();
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> deleteCompany(Long id) {
        // 根据业务需求，可能需要先删除关联的职位、申请等数据
        
        // 删除企业
        companyRepository.deleteById(id);
        
        // 删除用户
        userRepository.deleteById(id);
        
        return ResponseDTO.success();
    }

    @Override
    public ResponseDTO<PageDTO<JobDTO>> getCompanyJobs(Long id, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        
        Page<JobDTO> jobDTOPage = jobService.findByCompanyIdWithCompany(id, pageable);
        
        PageDTO<JobDTO> pageDTO = new PageDTO<>();
        pageDTO.setList(jobDTOPage.getContent());
        pageDTO.setTotal(jobDTOPage.getTotalElements());
        pageDTO.setPageNum(pageNum);
        pageDTO.setPageSize(pageSize);
        pageDTO.setPages(jobDTOPage.getTotalPages());
        
        return ResponseDTO.success(pageDTO);
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchVerifyCompanies(List<Long> ids, String status) {
        for (Long id : ids) {
            try {
                verifyCompany(id, status);
            } catch (Exception e) {
                // 记录错误并继续处理
                System.err.println("处理企业ID: " + id + " 出错: " + e.getMessage());
            }
        }
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchDisableCompanies(List<Long> ids) {
        ids.forEach(this::disableCompany);
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchEnableCompanies(List<Long> ids) {
        ids.forEach(this::enableCompany);
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchDeleteCompanies(List<Long> ids) {
        ids.forEach(this::deleteCompany);
        return ResponseDTO.success();
    }

    @Override
    public void exportCompanies(String name, String industry, String scale, String status, String sortBy, String sortDir, String startTime, String endTime, HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=companies.xlsx");
            
            // 查询企业列表（不分页）
            ResponseDTO<PageDTO<CompanyDTO>> result = getCompanyList(1, Integer.MAX_VALUE, name, industry, scale, status, sortBy, sortDir, startTime, endTime);
            List<CompanyDTO> companies = result.getData().getList();
            
            // 创建Excel并写入数据
            // ... 创建Excel的代码 ...
            
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException("导出企业数据失败", e);
        }
    }
    
    /**
     * 将实体转换为DTO
     * @param company 企业实体
     * @return 企业DTO
     */
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO companyDTO = new CompanyDTO();
        
        // 复制基本属性
        companyDTO.setId(company.getId());
        companyDTO.setName(company.getCompanyName());
        companyDTO.setIndustry(company.getIndustry());
        companyDTO.setScale(company.getScale());
        companyDTO.setLocation(company.getCity());
        companyDTO.setContactPerson(company.getContactPerson());
        
        // 获取关联的用户信息
        User user = company.getUser();
        if (user != null) {
            companyDTO.setEmail(user.getEmail());
            companyDTO.setPhone(user.getPhone());
            companyDTO.setStatus(user.getStatus().name());
            companyDTO.setRegisterTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 获取职位数量
        Long jobCount = jobRepository.count();
        companyDTO.setJobCount(jobCount.intValue());
        
        return companyDTO;
    }
} 