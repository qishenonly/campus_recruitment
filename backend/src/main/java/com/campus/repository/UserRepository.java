package com.campus.repository;

import com.campus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByEmail(String email);
    boolean existsByRealName(String username);
    Optional<User> findByUsername(String username);
} 