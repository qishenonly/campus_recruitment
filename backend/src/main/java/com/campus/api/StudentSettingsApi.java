package com.campus.api;

/**
 * 学生账号设置API接口
 */
public interface StudentSettingsApi {
    /**
     * 获取学生个人信息
     * GET /api/student/settings/profile
     * 
     * @param token 授权令牌
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "获取学生信息成功",
     *   "data": {
     *     "id": 1,
     *     "username": "student1",
     *     "realName": "张三",
     *     "avatar": "http://example.com/avatar/123.jpg",
     *     "email": "student1@example.com",
     *     "phone": "13800138000",
     *     "university": "清华大学",
     *     "major": "计算机科学与技术",
     *     "education": "本科",
     *     "graduationYear": "2025",
     *     "location": "北京",
     *     "introducation": "我是一名热爱技术的学生"
     *   }
     * }
     */
    
    /**
     * 更新学生个人信息
     * PUT /api/student/settings/profile
     * 
     * @param token 授权令牌
     * @param profileDTO 个人信息数据
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "更新学生信息成功",
     *   "data": {
     *     // 同上，返回更新后的学生信息
     *   }
     * }
     */
    
    /**
     * 上传学生头像
     * POST /api/student/settings/avatar
     * Content-Type: multipart/form-data
     * 
     * @param token 授权令牌
     * @param file 头像文件
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "头像上传成功",
     *   "data": {
     *     "url": "http://example.com/avatar/new-avatar.jpg"
     *   }
     * }
     */
    
    /**
     * 修改密码
     * POST /api/student/settings/password
     * 
     * @param token 授权令牌
     * @param passwordDTO 密码数据 {oldPassword, newPassword, confirmPassword}
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "修改密码成功",
     *   "data": true
     * }
     */
    
    /**
     * 发送手机验证码
     * POST /api/student/settings/send-phone-code?phone=13800138000
     * 
     * @param phone 手机号
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "验证码发送成功",
     *   "data": true
     * }
     */
    
    /**
     * 绑定手机号
     * POST /api/student/settings/bind-phone?phone=13800138000&code=1234
     * 
     * @param token 授权令牌
     * @param phone 手机号
     * @param code 验证码
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "手机号绑定成功",
     *   "data": true
     * }
     */
    
    /**
     * 发送邮箱验证码
     * POST /api/student/settings/send-email-code?email=student@example.com
     * 
     * @param email 邮箱地址
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "验证码发送成功",
     *   "data": true
     * }
     */
    
    /**
     * 绑定邮箱
     * POST /api/student/settings/bind-email?email=student@example.com&code=1234
     * 
     * @param token 授权令牌
     * @param email 邮箱地址
     * @param code 验证码
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "邮箱绑定成功",
     *   "data": true
     * }
     */
} 