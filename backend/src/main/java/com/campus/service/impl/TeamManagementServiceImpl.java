package com.campus.service.impl;

import com.campus.dto.TeamMemberDTO;
import com.campus.model.Company;
import com.campus.model.TeamMember;
import com.campus.model.User;
import com.campus.repository.CompanyRepository;
import com.campus.repository.TeamMemberRepository;
import com.campus.repository.UserRepository;
import com.campus.service.TeamManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamManagementServiceImpl implements TeamManagementService {

    private final TeamMemberRepository teamMemberRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据用户ID获取企业ID
     */
    private Long getCompanyIdByUserId(Long userId) {
        log.info("通过用户ID={}获取企业ID", userId);
        
        // 查找用户所属的团队成员记录
        TeamMember teamMember = teamMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("找不到该用户关联的企业信息"));
        
        // 获取关联的企业ID
        Long companyId = teamMember.getCompanyId();
        log.info("找到用户关联的企业ID={}", companyId);
        
        return companyId;
    }

    @Override
    public Page<TeamMember> getTeamMembers(Long userId, Pageable pageable) {
        log.info("获取用户ID={}的团队成员列表", userId);
        
        // 获取企业ID
        Long companyId = getCompanyIdByUserId(userId);
        
        // 检查公司是否存在
        companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        return teamMemberRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    @Transactional
    public TeamMember addTeamMember(Long userId, TeamMemberDTO memberDTO) {
        log.info("用户ID={}添加团队成员: {}", userId, memberDTO.getName());
        
        // 获取企业ID
        Long companyId = getCompanyIdByUserId(userId);
        
        // 检查公司是否存在
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        // 检查邮箱是否已存在于该公司
        if (teamMemberRepository.existsByCompanyIdAndEmail(companyId, memberDTO.getEmail())) {
            throw new IllegalArgumentException("该邮箱已被使用");
        }
        
        TeamMember teamMember = new TeamMember();
        teamMember.setName(memberDTO.getName());
        teamMember.setPosition(memberDTO.getPosition());
        teamMember.setEmail(memberDTO.getEmail());
        teamMember.setPhone(memberDTO.getPhone());
        teamMember.setRole(memberDTO.getRole());
        teamMember.setCreateTime(LocalDateTime.now());
        teamMember.setUpdateTime(LocalDateTime.now());
        
        // 设置企业关联
        teamMember.setCompany(company);
        
        // 如果提供了密码，创建用户账号
        if (memberDTO.getPassword() != null && !memberDTO.getPassword().isEmpty()) {
            // 检查密码长度和强度
            if (memberDTO.getPassword().length() < 6) {
                throw new IllegalArgumentException("密码长度不能少于6位");
            }
            
            // 检查两次密码是否一致
            if (!memberDTO.getPassword().equals(memberDTO.getConfirmPassword())) {
                throw new IllegalArgumentException("两次输入的密码不一致");
            }
            
            // 创建用户账号
            User user = new User();
            user.setUsername(memberDTO.getEmail()); // 使用邮箱作为用户名
            user.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
            user.setEmail(memberDTO.getEmail());
            user.setPhone(memberDTO.getPhone());
            user.setRealName(memberDTO.getName());
            user.setRole(User.UserRole.COMPANY); // 设置为企业角色
            user.setStatus(User.UserStatus.ACTIVE); // 设置为激活状态
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            teamMember.setUser(savedUser);
            teamMember.setUsername(savedUser.getUsername());
            
            log.info("为团队成员创建了用户账号: {}", savedUser.getUsername());
        }
        
        return teamMemberRepository.save(teamMember);
    }

    @Override
    @Transactional
    public TeamMember updateTeamMember(Long userId, Long memberId, TeamMemberDTO memberDTO) {
        log.info("用户ID={}更新团队成员ID={}", userId, memberId);
        
        // 获取企业ID
        Long companyId = getCompanyIdByUserId(userId);
        
        // 检查公司是否存在
        companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        // 检查团队成员是否存在
        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("团队成员不存在"));
        
        // 检查成员是否属于该公司
        if (!teamMember.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("该成员不属于此公司");
        }
        
        // 检查邮箱是否已被使用（排除自己）
        if (!teamMember.getEmail().equals(memberDTO.getEmail()) && 
                teamMemberRepository.existsByCompanyIdAndEmail(companyId, memberDTO.getEmail())) {
            throw new IllegalArgumentException("该邮箱已被使用");
        }
        
        // 更新基本信息
        teamMember.setName(memberDTO.getName());
        teamMember.setPosition(memberDTO.getPosition());
        teamMember.setEmail(memberDTO.getEmail());
        teamMember.setPhone(memberDTO.getPhone());
        teamMember.setRole(memberDTO.getRole());
        teamMember.setUpdateTime(LocalDateTime.now());
        
        // 如果有关联用户，同步更新用户信息
        User user = teamMember.getUser();
        if (user != null) {
            user.setEmail(memberDTO.getEmail());
            user.setPhone(memberDTO.getPhone());
            user.setRealName(memberDTO.getName());
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            log.info("同步更新了用户信息: {}", user.getUsername());
        }
        
        return teamMemberRepository.save(teamMember);
    }

    @Override
    @Transactional
    public void deleteTeamMember(Long userId, Long memberId) {
        log.info("用户ID={}删除团队成员ID={}", userId, memberId);
        
        // 获取企业ID
        Long companyId = getCompanyIdByUserId(userId);
        
        // 检查公司是否存在
        companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        // 检查团队成员是否存在
        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("团队成员不存在"));
        
        // 检查成员是否属于该公司
        if (!teamMember.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("该成员不属于此公司");
        }
        
        // 检查是否是管理员
        if ("admin".equals(teamMember.getRole())) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }
        
        // 删除团队成员记录
        teamMemberRepository.delete(teamMember);
        log.info("已删除团队成员: {}", teamMember.getName());
        
        // 如果有关联用户，注意不要删除用户账号，只是解除关联，防止误删除
    }

    @Override
    public TeamMember getTeamMember(Long userId, Long memberId) {
        log.info("用户ID={}获取团队成员ID={}", userId, memberId);
        
        // 获取企业ID
        Long companyId = getCompanyIdByUserId(userId);
        
        // 检查公司是否存在
        companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("企业信息不存在"));
        
        // 检查团队成员是否存在
        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("团队成员不存在"));
        
        // 检查成员是否属于该公司
        if (!teamMember.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("该成员不属于此公司");
        }
        
        return teamMember;
    }
} 