package com.campus.service.impl;

import com.campus.dto.CompanyDTO;
import com.campus.dto.JobDTO;
import com.campus.dto.PageDTO;
import com.campus.dto.ResponseDTO;
import com.campus.model.Company;
import com.campus.model.TeamMember;
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
                        // 查找管理员成员及其关联的用户
                        TeamMember adminMember = company.getTeamMembers().stream()
                                .filter(member -> "admin".equals(member.getRole()))
                                .findFirst()
                                .orElse(null);
                        
                        if (adminMember != null && adminMember.getUser() != null) {
                            match = match && adminMember.getUser().getStatus().name().equals(status);
                        } else {
                            match = false; // 没有管理员或用户，不符合状态筛选条件
                        }
                    }
                    
                    // 日期时间筛选
                    if (finalStart != null) {
                        match = match && company.getCreateTime() != null &&
                                company.getCreateTime().isAfter(finalStart);
                    }
                    
                    if (finalEnd != null) {
                        match = match && company.getCreateTime() != null &&
                                company.getCreateTime().isBefore(finalEnd);
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
        // 创建企业
        Company company = new Company();
        company.setCompanyName(companyDTO.getName());
        company.setIndustry(companyDTO.getIndustry());
        company.setScale(companyDTO.getScale());
        company.setCity(companyDTO.getLocation());
        company.setContactPerson(companyDTO.getContactPerson());
        company.setContactPosition("HR"); // 默认职位
        company.setVerified(false);
        company.setCreateTime(LocalDateTime.now());
        company.setUpdateTime(LocalDateTime.now());
        
        Company savedCompany = companyRepository.save(company);
        
        // 创建一个管理员团队成员
        if (companyDTO.getEmail() != null && !companyDTO.getEmail().isEmpty()) {
            TeamMember adminMember = new TeamMember();
            adminMember.setCompany(savedCompany);
            adminMember.setName(companyDTO.getContactPerson() != null ? companyDTO.getContactPerson() : "管理员");
            adminMember.setPosition("HR");
            adminMember.setEmail(companyDTO.getEmail());
            adminMember.setPhone(companyDTO.getPhone());
            adminMember.setRole("admin");
            adminMember.setCreateTime(LocalDateTime.now());
            adminMember.setUpdateTime(LocalDateTime.now());
            
            // 检查用户是否已存在
            User user;
            boolean userExists = userRepository.existsByEmail(companyDTO.getEmail());
            
            if (userExists) {
                // 如果存在，使用现有用户
                user = userRepository.findByEmail(companyDTO.getEmail());
                if (user == null) {
                    throw new RuntimeException("查找用户发生错误");
                }
            } else {
                // 如果不存在，创建新用户
                user = new User();
                user.setUsername(companyDTO.getEmail());
                user.setEmail(companyDTO.getEmail());
                user.setPhone(companyDTO.getPhone());
                user.setRealName(adminMember.getName());
                user.setPassword("123456"); // 初始密码
                user.setRole(User.UserRole.COMPANY);
                user.setStatus(User.UserStatus.ACTIVE);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                
                user = userRepository.save(user);
            }
            
            adminMember.setUser(user);
            adminMember.setUsername(user.getUsername());
            
            // 将团队成员添加到企业
            savedCompany.addTeamMember(adminMember);
            savedCompany = companyRepository.save(savedCompany);
        }
        
        return ResponseDTO.success(savedCompany.getId());
    }

    @Override
    @Transactional
    public ResponseDTO<Void> updateCompany(Long id, CompanyDTO companyDTO) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
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
        
        company.setUpdateTime(LocalDateTime.now());
        companyRepository.save(company);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> verifyCompany(Long id, String status) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        // 查找管理员成员及其关联的用户
        TeamMember adminMember = company.getTeamMembers().stream()
                .filter(member -> "admin".equals(member.getRole()))
                .findFirst()
                .orElse(null);
        
        if (adminMember == null || adminMember.getUser() == null) {
            return ResponseDTO.error("企业管理员账号不存在");
        }
        
        User user = adminMember.getUser();
        
        if (status.equals("ACTIVE")) {
            company.setVerified(true);
            user.setStatus(User.UserStatus.ACTIVE);
        } else if (status.equals("INACTIVE")) {
            company.setVerified(false);
            user.setStatus(User.UserStatus.INACTIVE);
        } else if (status.equals("BLOCKED")) {
            user.setStatus(User.UserStatus.BLOCKED);
        } else {
            return ResponseDTO.error("无效的状态");
        }
        
        companyRepository.save(company);
        userRepository.save(user);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> disableCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        // 查找管理员成员及其关联的用户
        TeamMember adminMember = company.getTeamMembers().stream()
                .filter(member -> "admin".equals(member.getRole()))
                .findFirst()
                .orElse(null);
        
        if (adminMember != null && adminMember.getUser() != null) {
            User user = adminMember.getUser();
            user.setStatus(User.UserStatus.BLOCKED);
            userRepository.save(user);
        }
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> enableCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        // 查找管理员成员及其关联的用户
        TeamMember adminMember = company.getTeamMembers().stream()
                .filter(member -> "admin".equals(member.getRole()))
                .findFirst()
                .orElse(null);
        
        if (adminMember != null && adminMember.getUser() != null) {
            User user = adminMember.getUser();
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
        }
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("企业不存在"));
        
        // 查找所有的团队成员及其关联的用户
        List<TeamMember> teamMembers = company.getTeamMembers();
        List<Long> userIds = teamMembers.stream()
                .filter(member -> member.getUser() != null)
                .map(member -> member.getUser().getId())
                .collect(Collectors.toList());
        
        // 删除企业（会级联删除团队成员）
        companyRepository.deleteById(id);
        
        // 删除关联的用户账号
        for (Long userId : userIds) {
            try {
                userRepository.deleteById(userId);
            } catch (Exception e) {
                // 记录错误但继续执行
                System.err.println("删除用户ID: " + userId + " 出错: " + e.getMessage());
            }
        }
        
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
        
        // 查找管理员成员
        TeamMember adminMember = company.getTeamMembers().stream()
                .filter(member -> "admin".equals(member.getRole()))
                .findFirst()
                .orElse(null);
        
        if (adminMember != null && adminMember.getUser() != null) {
            User user = adminMember.getUser();
            companyDTO.setEmail(user.getEmail());
            companyDTO.setPhone(user.getPhone());
            companyDTO.setStatus(user.getStatus().name());
            companyDTO.setRegisterTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            // 如果没有管理员成员，使用企业的创建时间
            companyDTO.setRegisterTime(company.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 获取职位数量
        Long jobCount = jobRepository.count();
        companyDTO.setJobCount(jobCount.intValue());
        
        return companyDTO;
    }
} 