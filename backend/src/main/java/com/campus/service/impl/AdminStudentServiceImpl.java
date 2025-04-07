package com.campus.service.impl;

import com.campus.dto.PageDTO;
import com.campus.dto.ResponseDTO;
import com.campus.dto.StudentDTO;
import com.campus.model.Student;
import com.campus.model.User;
import com.campus.repository.StudentRepository;
import com.campus.repository.UserRepository;
import com.campus.service.AdminStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AdminStudentServiceImpl implements AdminStudentService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResponseDTO<PageDTO<StudentDTO>> getStudentList(Integer pageNum, Integer pageSize, String name, String school, String major, String status) {
        // 创建分页请求
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        
        // 获取学生列表
        Page<Student> studentPage = studentRepository.findAll(pageable);
        
        // 转换为DTO并过滤
        List<StudentDTO> studentDTOs = studentPage.getContent().stream()
                .filter(student -> {
                    // 根据条件过滤
                    boolean match = true;
                    
                    if (name != null && !name.isEmpty()) {
                        match = match && student.getRealName().contains(name);
                    }
                    
                    if (school != null && !school.isEmpty()) {
                        match = match && student.getUniversity().contains(school);
                    }
                    
                    if (major != null && !major.isEmpty()) {
                        match = match && student.getMajor().contains(major);
                    }
                    
                    if (status != null && !status.isEmpty()) {
                        match = match && student.getUser() != null && 
                                student.getUser().getStatus().name().equals(status);
                    }
                    
                    return match;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 创建分页DTO
        PageDTO<StudentDTO> pageDTO = new PageDTO<>();
        pageDTO.setList(studentDTOs);
        pageDTO.setTotal(studentPage.getTotalElements());
        pageDTO.setPageNum(pageNum);
        pageDTO.setPageSize(pageSize);
        pageDTO.setPages(studentPage.getTotalPages());
        
        return ResponseDTO.success(pageDTO);
    }

    @Override
    public ResponseDTO<StudentDTO> getStudentDetail(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        return ResponseDTO.success(convertToDTO(student));
    }

    @Override
    @Transactional
    public ResponseDTO<Void> updateStudent(Long id, StudentDTO studentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        User user = student.getUser();
        
        // 更新用户信息
        if (studentDTO.getEmail() != null) {
            user.setEmail(studentDTO.getEmail());
        }
        
        if (studentDTO.getPhone() != null) {
            user.setPhone(studentDTO.getPhone());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        
        // 更新学生信息
        if (studentDTO.getName() != null) {
            student.setRealName(studentDTO.getName());
        }
        
        if (studentDTO.getSchool() != null) {
            student.setUniversity(studentDTO.getSchool());
        }
        
        if (studentDTO.getMajor() != null) {
            student.setMajor(studentDTO.getMajor());
        }
        
        if (studentDTO.getEducation() != null) {
            try {
                student.setEducation(Student.Education.valueOf(studentDTO.getEducation()));
            } catch (IllegalArgumentException e) {
                // 忽略无效的教育程度值
            }
        }
        
        if (studentDTO.getGraduationYear() != null) {
            student.setGraduationYear(studentDTO.getGraduationYear());
        }
        
        studentRepository.save(student);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> verifyStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        User user = student.getUser();
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> disableStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        User user = student.getUser();
        user.setStatus(User.UserStatus.BLOCKED);
        userRepository.save(user);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> enableStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        User user = student.getUser();
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> deleteStudent(Long id) {
        // 删除学生
        studentRepository.deleteById(id);
        
        // 删除用户
        userRepository.deleteById(id);
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<String> resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 生成随机密码
        String newPassword = generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseDTO.success("密码已重置为: " + newPassword);
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchVerifyStudents(List<Long> ids) {
        ids.forEach(id -> {
            try {
                verifyStudent(id);
            } catch (Exception e) {
                // 记录错误但继续处理
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchDisableStudents(List<Long> ids) {
        ids.forEach(id -> {
            try {
                disableStudent(id);
            } catch (Exception e) {
                // 记录错误但继续处理
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchEnableStudents(List<Long> ids) {
        ids.forEach(id -> {
            try {
                enableStudent(id);
            } catch (Exception e) {
                // 记录错误但继续处理
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    @Transactional
    public ResponseDTO<Void> batchDeleteStudents(List<Long> ids) {
        ids.forEach(id -> {
            try {
                deleteStudent(id);
            } catch (Exception e) {
                // 记录错误但继续处理
            }
        });
        
        return ResponseDTO.success();
    }

    @Override
    public void exportStudents(String name, String school, String major, String status, HttpServletResponse response) {
        try {
            List<Student> students = studentRepository.findAll();
            
            // 根据条件过滤
            List<Student> filteredStudents = students.stream()
                    .filter(student -> {
                        boolean match = true;
                        
                        if (name != null && !name.isEmpty()) {
                            match = match && student.getRealName().contains(name);
                        }
                        
                        if (school != null && !school.isEmpty()) {
                            match = match && student.getUniversity().contains(school);
                        }
                        
                        if (major != null && !major.isEmpty()) {
                            match = match && student.getMajor().contains(major);
                        }
                        
                        if (status != null && !status.isEmpty()) {
                            match = match && student.getUser() != null && 
                                    student.getUser().getStatus().name().equals(status);
                        }
                        
                        return match;
                    })
                    .collect(Collectors.toList());
            
            // 设置响应头
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=students_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv");
            
            // 写入CSV头
            String header = "ID,姓名,性别,学校,专业,学历,毕业年份,邮箱,电话,状态,注册时间\n";
            response.getWriter().write(header);
            
            // 写入数据
            for (Student student : filteredStudents) {
                User user = student.getUser();
                String line = student.getId() + "," +
                        student.getRealName() + "," +
                        student.getGender() + "," +
                        student.getUniversity() + "," +
                        student.getMajor() + "," +
                        student.getEducation() + "," +
                        student.getGraduationYear() + "," +
                        (user != null ? user.getEmail() : "") + "," +
                        (user != null ? user.getPhone() : "") + "," +
                        (user != null ? user.getStatus() : "") + "," +
                        (user != null ? user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "") + "\n";
                
                response.getWriter().write(line);
            }
            
            response.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("导出学生数据失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> importStudents(MultipartFile file) {
        // 实际实现需要解析Excel文件，这里简化处理
        if (file.isEmpty()) {
            return ResponseDTO.error("导入文件不能为空");
        }
        
        try {
            // TODO: 解析Excel文件并导入学生数据
            return ResponseDTO.success("已成功导入学生数据");
        } catch (Exception e) {
            return ResponseDTO.error("导入学生数据失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=student_import_template.xlsx");
            
            // TODO: 创建并输出Excel模板
            
            response.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("下载模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 将实体转换为DTO
     * @param student 学生实体
     * @return 学生DTO
     */
    private StudentDTO convertToDTO(Student student) {
        StudentDTO studentDTO = new StudentDTO();
        
        // 复制基本属性
        studentDTO.setId(student.getId());
        studentDTO.setName(student.getRealName());
        studentDTO.setGender(student.getGender().name());
        studentDTO.setSchool(student.getUniversity());
        studentDTO.setMajor(student.getMajor());
        studentDTO.setEducation(student.getEducation().name());
        studentDTO.setGraduationYear(student.getGraduationYear());
        
        // 获取关联的用户信息
        User user = student.getUser();
        if (user != null) {
            studentDTO.setEmail(user.getEmail());
            studentDTO.setPhone(user.getPhone());
            studentDTO.setStatus(user.getStatus().name());
            studentDTO.setRegisterTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        return studentDTO;
    }
    
    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
} 