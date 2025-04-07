package com.campus.repository;

import com.campus.model.Company;
import com.campus.model.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    // 根据公司查询
    List<TeamMember> findByCompany(Company company);
    
    // 使用自定义查询替换 findByCompanyId
    @Query("SELECT t FROM TeamMember t WHERE t.company.id = :companyId")
    List<TeamMember> findByCompanyId(@Param("companyId") Long companyId);
    
    // 分页查询
    Page<TeamMember> findByCompany(Company company, Pageable pageable);
    
    // 使用自定义查询替换 findByCompanyId
    @Query("SELECT t FROM TeamMember t WHERE t.company.id = :companyId")
    Page<TeamMember> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
    
    // 查询特定角色的成员
    Optional<TeamMember> findByCompanyAndRole(Company company, String role);
    
    // 使用自定义查询替换 findByCompanyIdAndRole
    @Query("SELECT t FROM TeamMember t WHERE t.company.id = :companyId AND t.role = :role")
    Optional<TeamMember> findByCompanyIdAndRole(@Param("companyId") Long companyId, @Param("role") String role);
    
    // 检查邮箱是否已存在
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TeamMember t WHERE t.company.id = :companyId AND t.email = :email")
    boolean existsByCompanyIdAndEmail(@Param("companyId") Long companyId, @Param("email") String email);
    
    // 根据邮箱查询
    Optional<TeamMember> findByEmail(String email);
    
    // 使用自定义查询替换 findByUserId
    @Query("SELECT t FROM TeamMember t WHERE t.user.id = :userId")
    Optional<TeamMember> findByUserId(@Param("userId") Long userId);
} 