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

-- 企业表
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(100) NOT NULL,
    industry VARCHAR(50) NOT NULL,
    scale VARCHAR(50),
    description TEXT,
    location VARCHAR(200),
    website VARCHAR(200),
    logo VARCHAR(200),
    verified BOOLEAN DEFAULT FALSE,
    verification_files VARCHAR(500),
    contact_person VARCHAR(50),
    contact_position VARCHAR(50),
    FOREIGN KEY (id) REFERENCES users(id)
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
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 申请表
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    apply_time DATETIME NOT NULL,
    status ENUM('PENDING', 'VIEWED', 'INTERVIEW', 'OFFER', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    feedback TEXT,
    last_update_time DATETIME NOT NULL,
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (resume_id) REFERENCES resumes(id)
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

-- 对话表
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
    FOREIGN KEY (company_id) REFERENCES users(id)
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
    application_id BIGINT NOT NULL,
    interview_time DATETIME NOT NULL,
    duration INT NOT NULL, -- 面试时长（分钟）
    type ENUM('ONLINE', 'OFFLINE') NOT NULL,
    location VARCHAR(200), -- 线下面试地点
    online_url VARCHAR(200), -- 线上面试链接
    description TEXT, -- 面试说明
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELED') NOT NULL DEFAULT 'PENDING',
    feedback TEXT, -- 面试反馈
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (application_id) REFERENCES applications(id)
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

-- 插入示例数据
-- 1. 插入用户
INSERT INTO users (id, username, password, email, phone, role, create_time, update_time, status) VALUES
(1,'student1', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'student1@university.edu', '13800138001', 'STUDENT', NOW(), NOW(), 'ACTIVE'),
(2,'student2', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'student2@university.edu', '13800138002', 'STUDENT', NOW(), NOW(), 'ACTIVE'),
(3,'student3', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'student3@university.edu', '13800138003', 'STUDENT', NOW(), NOW(), 'ACTIVE'),
(4,'student4', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'student4@university.edu', '13800138004', 'STUDENT', NOW(), NOW(), 'ACTIVE'),
(5,'student5', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'student5@university.edu', '13800138005', 'STUDENT', NOW(), NOW(), 'ACTIVE'),
(6,'赵钱', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'hr@company1.com', '13800138002', 'COMPANY', NOW(), NOW(), 'ACTIVE'),
(7,'孙吴', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'hr@company2.com', '13800138003', 'COMPANY', NOW(), NOW(), 'ACTIVE'),
(8,'浅仓', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'hr@company3.com', '13800138004', 'COMPANY', NOW(), NOW(), 'ACTIVE'),
(9,'华青', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'hr@company4.com', '13800138005', 'COMPANY', NOW(), NOW(), 'ACTIVE'),
(10,'成汗', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'hr@company5.com', '13800138006', 'COMPANY', NOW(), NOW(), 'ACTIVE'),
(11,'admin1', '$2a$10$xVWZxKFGZgRqf1sAhQWoUOYYok.KzX4hpWH5c3lxE6HxuVb3pbhPS', 'admin@system.com', '13800138003', 'ADMIN', NOW(), NOW(), 'ACTIVE');

-- 2. 插入学生信息
INSERT INTO students (id, real_name, university, major, education, graduation_year, gender, birth, location, expected_position, expected_salary, expected_city) VALUES
(1, '张三', '北京大学', '计算机科学', '本科', '2024', '男', '2000-01-01', '北京', '后端开发工程师', '15k-20k', '北京'),
(2, '李四', '清华大学', '计算机科学', '本科', '2024', '男', '2000-01-01', '北京', '后端开发工程师', '15k-20k', '北京'),
(3, '王五', '复旦大学', '计算机科学', '本科', '2024', '男', '2000-01-01', '北京', '后端开发工程师', '15k-20k', '北京'),
(4, '赵六', '上海交通大学', '计算机科学', '本科', '2024', '男', '2000-01-01', '北京', '后端开发工程师', '15k-20k', '北京'),
(5, '孙七', '浙江大学', '计算机科学', '本科', '2024', '男', '2000-01-01', '北京', '后端开发工程师', '15k-20k', '北京');


-- 3. 插入企业信息
INSERT INTO companies (id, company_name, industry, scale, description, location, website, logo, verified, contact_person, contact_position) VALUES
(1, '小米', '互联网', '100-499人', '一家创新的科技公司', '北京市海淀区', 'https://www.mi.com/', 'https://upload.wikimedia.org/wikipedia/commons/a/ae/Xiaomi_logo_%282021-%29.svg', TRUE, '李四', 'HR经理'),
(2, '华为', '互联网', '100-499人', '一家创新的科技公司', '北京市海淀区', 'https://www.huawei.com/cn/', 'https://www.logobiaozhi.com/upLoad/image/20230909/16942489332742399.jpg', TRUE, '李四', 'HR经理'),
(3, '腾讯', '互联网', '100-499人', '一家创新的科技公司', '北京市海淀区', 'https://www.tencent.com/zh-cn/', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSuKyPBLu9Tc8WmHglxrEEgBxl2B0E3crBNIQ&s', TRUE, '李四', 'HR经理'),
(4, '阿里巴巴', '互联网', '100-499人', '一家创新的科技公司', '北京市海淀区', 'https://alibaba.com.cn/', 'https://logores.yrucd.com/wp-content/uploads/2023/01/Alibaba_logo_PNG2.png!a', TRUE, '李四', 'HR经理'),
(5, '字节跳动', '互联网', '100-499人', '一家创新的科技公司', '北京市海淀区', 'https://www.bytedance.com/zh/', 'https://puui.qpic.cn/vpic_cover/v31474h4bpo/v31474h4bpo_hz.jpg', TRUE, '李四', 'HR经理');

-- 4. 插入职位信息
INSERT INTO jobs (company_id, publisher_id, publisher_name, publisher_position, title, description, requirements, salary, location, position_type, education_requirement, major_requirement, publish_date, deadline) VALUES
(1, 6, '赵钱', 'HR', 'Java后端开发工程师', '负责公司核心系统开发', '1. 本科及以上学历\n2. 熟悉Java编程\n3. 了解Spring框架', '15k-25k', '北京', '全职', '本科', '计算机相关专业', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, 7, '孙吴', 'HRBP', 'Python后端开发工程师', '负责公司核心系统开发', '1. 本科及以上学历\n2. 熟悉Python编程\n3. 了解Django框架', '15k-25k', '北京', '全职', '本科', '计算机相关专业', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(3, 8, '浅仓', 'HR', '前端开发工程师', '负责公司核心系统开发', '1. 本科及以上学历\n2. 熟悉Vue.js编程\n3. 了解React框架', '15k-25k', '北京', '全职', '本科', '计算机相关专业', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(4, 9, '华青', 'HRBP', 'UI设计师', '负责公司核心系统开发', '1. 本科及以上学历\n2. 熟悉UI设计\n3. 了解Photoshop', '15k-25k', '北京', '全职', '本科', '计算机相关专业', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(5, 10, '成汗', 'HR', '产品经理', '负责公司核心系统开发', '1. 本科及以上学历\n2. 熟悉产品经理', '15k-25k', '北京', '全职', '本科', '计算机相关专业', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

-- 5. 插入简历信息
INSERT INTO resumes (student_id, name, education, experience, skills, projects, awards, self_evaluation, create_time, update_time) VALUES
(1, '我的简历', '北京大学 计算机科学专业 本科', '某公司实习经历', 'Java, Spring Boot, MySQL', '校园项目经验', '奖学金获得者', '积极主动，学习能力强', NOW(), NOW());

-- 6. 插入申请记录
INSERT INTO applications (job_id, student_id, resume_id, apply_time, status, last_update_time) VALUES
(1, 1, 1, NOW(), 'PENDING', NOW());

-- 7. 插入通知信息
INSERT INTO notifications (user_id, title, content, type, related_id, create_time) VALUES
(1, '简历投递成功', '您已成功投递Java后端开发工程师职位', 'APPLICATION', 1, NOW());