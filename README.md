# 校园招聘系统

一个基于Vue 3 + Spring Boot的现代化校园招聘系统,为企业和学生提供招聘服务平台。

## 功能特点

### 学生用户
- 账号管理
  - 邮箱注册/登录
  - 个人信息完善(姓名、学校、专业等)
  - 密码修改与找回
  - 实名认证
- 简历管理
  - 在线创建/编辑简历
  - 上传PDF格式简历
  - 简历预览与下载
  - 多份简历管理
- 职位搜索
  - 多维度搜索(职位名称、公司、地点等)
  - 条件筛选(薪资、学历要求、工作经验等) 
  - 职位收藏与订阅
  - 相似职位推荐
- 职位申请
  - 简历投递
  - 申请进度跟踪
  - 面试通知
  - offer管理
- 消息中心
  - 系统通知
  - 企业沟通
  - 面试邀请
  - 站内信

### 企业用户
- 企业管理
  - 企业信息维护
  - 企业认证(营业执照等)
  - 招聘团队管理
  - 企业主页定制
- 职位管理
  - 发布职位
  - 职位状态管理(上下架)
  - 申请者管理
  - 职位统计分析
- 简历管理
  - 简历筛选
  - 简历标签管理
  - 简历下载
  - 简历搜索
- 消息通知
  - 与候选人沟通
  - 发送面试邀请
  - 系统通知管理
  - 邮件模板配置

## 技术栈

### 前端
- Vue 3 - 渐进式JavaScript框架
- Vite - 前端构建工具
- Vue Router - 路由管理
- Pinia - 状态管理
- Vant UI - 移动端组件库
- Element Plus - PC端组件库
- Axios - HTTP请求库
- ECharts - 数据可视化
- Sass - CSS预处理器
- ESLint - 代码规范
- Prettier - 代码格式化

### 后端
- Spring Boot - 应用开发框架
- Spring Security - 安全框架
- Spring Data JPA - 数据访问
- MySQL - 关系型数据库
- Redis - 缓存数据库
- JWT - 身份认证
- Maven - 项目管理工具
- Swagger - API文档
- Log4j - 日志管理
- JUnit - 单元测试

## 环境要求

- Node.js 16+
- JDK 1.8+
- MySQL 5.7+
- Redis 6.0+
- Maven 3.6+

## 项目结构

```
.
├── frontend/               # 前端项目
│   ├── src/
│   │   ├── api/           # API接口
│   │   ├── assets/        # 静态资源
│   │   ├── components/    # 公共组件
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # 状态管理
│   │   ├── styles/        # 全局样式
│   │   ├── utils/         # 工具函数
│   │   └── views/         # 页面组件
│   ├── .env               # 环境变量
│   ├── package.json       # 项目配置
│   └── vite.config.js     # Vite配置
│
└── backend/               # 后端项目
    ├── src/
    │   └── main/
    │       ├── java/
    │       │   └── com/campus/
    │       │       ├── config/     # 配置类
    │       │       ├── controller/ # 控制器
    │       │       ├── model/      # 实体类
    │       │       ├── repository/ # 数据访问
    │       │       ├── service/    # 业务逻辑
    │       │       └── util/       # 工具类
    │       └── resources/          # 配置文件
    ├── pom.xml            # Maven配置
    └── README.md          # 后端说明
```

## 快速开始

### 环境准备
1. 安装Node.js、JDK、MySQL、Redis
2. 克隆项目到本地
3. 创建数据库并导入SQL文件
4. 修改后端配置文件

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 后端启动
```bash
cd backend
mvn spring-boot:run
```

## 数据库设计

主要数据表:
- users - 用户表(id, username, password, email, phone, role, status等)
- students - 学生信息表(user_id, name, school, major, graduation_year等)
- companies - 企业信息表(user_id, name, industry, scale, description等)
- jobs - 职位表(id, company_id, title, description, requirements, salary等)
- resumes - 简历表(id, student_id, title, content, status等)
- applications - 申请记录表(id, job_id, student_id, resume_id, status等)
- conversations - 会话表(id, job_id, company_id, student_id等)
- messages - 消息表(id, conversation_id, sender_id, content, type等)
- notifications - 通知表(id, user_id, title, content, type, status等)

## API文档

### 认证相关
- POST /api/auth/register - 用户注册
- POST /api/auth/login - 用户登录
- POST /api/auth/logout - 用户登出
- POST /api/auth/send-verification - 发送验证码
- POST /api/auth/reset-password - 重置密码

### 用户相关
- GET /api/user/profile - 获取用户信息
- POST /api/user/profile - 更新用户信息
- POST /api/user/avatar - 上传头像
- POST /api/user/password - 修改密码

### 职位相关
- GET /api/jobs - 获取职位列表
- GET /api/jobs/{id} - 获取职位详情
- POST /api/jobs - 发布职位
- PUT /api/jobs/{id} - 更新职位
- DELETE /api/jobs/{id} - 删除职位
- POST /api/jobs/{id}/apply - 申请职位

### 简历相关
- POST /api/resumes/upload - 上传简历
- GET /api/resumes - 获取简历列表
- GET /api/resumes/{id} - 获取简历详情
- PUT /api/resumes/{id} - 更新简历
- DELETE /api/resumes/{id} - 删除简历

## 部署说明

### 开发环境
1. 前端运行在 http://localhost:10000
2. 后端运行在 http://localhost:8080
3. 数据库运行在 localhost:3306
4. Redis运行在 localhost:6379

### 生产环境
1. 使用Nginx部署前端静态资源
2. 使用Docker容器化部署后端服务
3. 配置HTTPS证书
4. 设置跨域和反向代理

## 贡献指南

1. Fork 项目
2. 创建特性分支 (git checkout -b feature/AmazingFeature)
3. 提交更改 (git commit -m 'Add some AmazingFeature')
4. 推送到分支 (git push origin feature/AmazingFeature)
5. 发起 Pull Request

## 开源协议

MIT License

## 联系我们

- 项目地址: https://github.com/fire-wq/campus-recruitment
- 问题反馈: https://github.com/fire-wq/campus-recruitment/issues

## 更新日志

### v1.0.0 (2024-03-20)
- 初始版本发布
- 实现基础功能
- 完善文档

## 致谢

感谢所有贡献者对项目的支持!
