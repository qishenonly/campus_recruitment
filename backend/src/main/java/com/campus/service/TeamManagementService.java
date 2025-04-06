package com.campus.service;

import com.campus.dto.TeamMemberDTO;
import com.campus.model.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamManagementService {
    /**
     * 获取公司团队成员列表
     * @param companyId 公司ID
     * @param pageable 分页参数
     * @return 团队成员分页列表
     */
    Page<TeamMember> getTeamMembers(Long companyId, Pageable pageable);
    
    /**
     * 添加团队成员
     * @param companyId 公司ID
     * @param memberDTO 团队成员DTO
     * @return 添加的团队成员
     */
    TeamMember addTeamMember(Long companyId, TeamMemberDTO memberDTO);
    
    /**
     * 更新团队成员
     * @param companyId 公司ID
     * @param memberId 成员ID
     * @param memberDTO 团队成员DTO
     * @return 更新后的团队成员
     */
    TeamMember updateTeamMember(Long companyId, Long memberId, TeamMemberDTO memberDTO);
    
    /**
     * 删除团队成员
     * @param companyId 公司ID
     * @param memberId 成员ID
     */
    void deleteTeamMember(Long companyId, Long memberId);
    
    /**
     * 获取团队成员详情
     * @param companyId 公司ID
     * @param memberId 成员ID
     * @return 团队成员
     */
    TeamMember getTeamMember(Long companyId, Long memberId);
} 