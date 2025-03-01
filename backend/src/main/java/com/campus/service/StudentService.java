package com.campus.service;

import com.campus.model.Student;
import com.campus.model.User;
import com.campus.repository.StudentRepository;
import com.campus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;

    public Student save(Long userId, Student student) {
        // 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // 设置用户关联
        student.setUser(user);
        
        return studentRepository.save(student);
    }

    public Optional<Student> findByUserId(Long userId) {
        return studentRepository.findById(userId);
    }
} 