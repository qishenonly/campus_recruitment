-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_recruitment;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role ENUM('STUDENT', 'COMPANY', 'ADMIN') NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') NOT NULL DEFAULT 'ACTIVE'
);

-- 学生表
CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    real_name VARCHAR(50) NOT NULL,
    university VARCHAR(100) NOT NULL,
    major VARCHAR(100) NOT NULL,
    education ENUM('专科', '本科', '硕士', '博士') NOT NULL,
    graduation_year VARCHAR(4) NOT NULL,
    gender ENUM('男', '女', '保密') DEFAULT '保密',
    birth DATE,
    location VARCHAR(100),
    expected_position VARCHAR(100),
    expected_salary VARCHAR(50),
    expected_city VARCHAR(100),
    FOREIGN KEY (id) REFERENCES users(id)
);

-- 企业表（已修改：移除与用户表的外键约束，添加创建时间和更新时间字段）
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(100) NOT NULL,
    industry VARCHAR(50) NOT NULL,
    scale VARCHAR(50),
    description TEXT,
    city VARCHAR(100),
    address VARCHAR(200),
    website VARCHAR(200),
    logo VARCHAR(200),
    verified BOOLEAN DEFAULT FALSE,
    verification_files VARCHAR(500),
    contact_person VARCHAR(50),
    contact_position VARCHAR(50),
    financing_stage VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 企业团队成员表（新增）
CREATE TABLE team_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'member' NOT NULL,
    username VARCHAR(50),
    user_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 职位表
CREATE TABLE jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    publisher_id BIGINT NOT NULL,  -- 已有：发布者ID
    publisher_name VARCHAR(50),     -- 新增：发布者姓名
    publisher_position VARCHAR(50), -- 新增：发布者职位
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    salary VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    position_type ENUM('全职', '实习', '兼职') NOT NULL,
    education_requirement VARCHAR(50),
    major_requirement VARCHAR(100),
    publish_date DATETIME NOT NULL,
    deadline DATETIME NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED', 'DELETED') NOT NULL DEFAULT 'PUBLISHED',
    view_count INT DEFAULT 0,
    apply_count INT DEFAULT 0,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (publisher_id) REFERENCES users(id)
);

-- 简历表
CREATE TABLE resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    education TEXT,
    experience TEXT,
    skills TEXT,
    projects TEXT,
    awards TEXT,
    self_evaluation TEXT,
    attachment_url VARCHAR(200),
    content TEXT,
    position_applied VARCHAR(100),
    status ENUM('待处理', '已查看', '面试中', '已录用', '已拒绝') NOT NULL DEFAULT '待处理',
    phone VARCHAR(20),
    email VARCHAR(100),
    school VARCHAR(100),
    major VARCHAR(100),
    graduate_year VARCHAR(4),
    submit_time DATETIME,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 通知表
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type ENUM('SYSTEM', 'APPLICATION', 'MESSAGE', 'INTERVIEW') NOT NULL,
    related_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 职位申请表
CREATE TABLE job_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    cover_letter TEXT,
    status ENUM('PENDING', 'REVIEWING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (resume_id) REFERENCES resumes(id),
    UNIQUE KEY `uk_student_job` (`student_id`, `job_id`)  -- 防止重复投递
);

-- 对话表（已修改：企业ID引用修改为companies表）
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (application_id) REFERENCES job_applications(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 消息表
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 面试表
CREATE TABLE interviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT,
    interview_time DATETIME NOT NULL,
    duration INT NOT NULL, -- 面试时长（分钟）
    type ENUM('ONLINE', 'ONSITE', "PHONE") NOT NULL,
    location VARCHAR(200), -- 线下面试地点
    online_url VARCHAR(200), -- 线上面试链接
    description TEXT, -- 面试说明
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELED') NOT NULL DEFAULT 'PENDING',
    feedback TEXT, -- 面试反馈
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    candidate_email VARCHAR(100), -- 候选人邮箱
    FOREIGN KEY (application_id) REFERENCES job_applications(id)
);

-- 职位收藏表
CREATE TABLE job_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    UNIQUE KEY `uk_student_job` (`student_id`, `job_id`)  -- 防止重复收藏
);

-- 系统操作日志表
CREATE TABLE system_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    content TEXT COMMENT '操作内容',
    operator_name VARCHAR(100) NOT NULL COMMENT '操作者姓名',
    operator_id BIGINT COMMENT '操作者ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME NOT NULL COMMENT '操作时间'
);

-- 初始化管理员账号
-- 注意：密码使用加密方式存储，这里使用BCrypt加密方式，admin123的加密后的结果
INSERT INTO users (username, password, email, phone, role, create_time, update_time, status) 
VALUES ('admin', '$2a$10$oc0Uv5zlO6rOZiMWxP1JZ.lMM4oRohrY5dq0UTCOKFNhAgPnkUo0G', 'admin@campus.com', '13800000000', 'ADMIN', NOW(), NOW(), 'ACTIVE')
ON DUPLICATE KEY UPDATE update_time = NOW();

