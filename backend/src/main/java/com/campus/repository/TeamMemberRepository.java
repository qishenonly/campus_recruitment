package com.campus.repository;

import com.campus.model.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByCompanyId(Long companyId);
    
    Page<TeamMember> findByCompanyId(Long companyId, Pageable pageable);
    
    Optional<TeamMember> findByCompanyIdAndRole(Long companyId, String role);
    
    boolean existsByCompanyIdAndEmail(Long companyId, String email);
    
    Optional<TeamMember> findByEmail(String email);
    
    Optional<TeamMember> findByUserId(Long userId);
} 