package com.campus.controller;

import com.campus.dto.TeamMemberDTO;
import com.campus.model.TeamMember;
import com.campus.service.TeamManagementService;
import com.campus.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company/team")
public class TeamManagementController {

    private final TeamManagementService teamManagementService;

    /**
     * 获取团队成员列表
     */
    @GetMapping
    public ResponseEntity<?> getTeamMembers(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 配置分页
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("role").descending().and(Sort.by("createTime").descending()));
            
            // 获取团队成员
            Page<TeamMember> teamMembers = teamManagementService.getTeamMembers(userId, pageable);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", teamMembers.getContent());
            data.put("total", teamMembers.getTotalElements());
            data.put("pages", teamMembers.getTotalPages());
            data.put("size", teamMembers.getSize());
            data.put("current", teamMembers.getNumber() + 1);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取团队成员成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取团队成员失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取团队成员失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 添加团队成员
     */
    @PostMapping
    public ResponseEntity<?> addTeamMember(
            @RequestHeader("Authorization") String token,
            @RequestBody TeamMemberDTO teamMemberDTO) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 添加团队成员
            TeamMember teamMember = teamManagementService.addTeamMember(userId, teamMemberDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "添加团队成员成功");
            response.put("data", teamMember);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("添加团队成员失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "添加团队成员失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 更新团队成员
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeamMember(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long memberId,
            @RequestBody TeamMemberDTO teamMemberDTO) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 更新团队成员
            TeamMember teamMember = teamManagementService.updateTeamMember(userId, memberId, teamMemberDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新团队成员成功");
            response.put("data", teamMember);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新团队成员失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新团队成员失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除团队成员
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeamMember(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long memberId) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 删除团队成员
            teamManagementService.deleteTeamMember(userId, memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除团队成员成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除团队成员失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除团队成员失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取团队成员详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamMember(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long memberId) {
        try {
            // 从JWT中获取用户ID
            String jwtToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(jwtToken);
            
            // 获取团队成员详情
            TeamMember teamMember = teamManagementService.getTeamMember(userId, memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取团队成员详情成功");
            response.put("data", teamMember);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取团队成员详情失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取团队成员详情失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 