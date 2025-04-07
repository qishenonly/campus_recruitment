package com.campus.api;

/**
 * 职位与公司相关API接口
 */
public interface JobCompanyApi {
    /**
     * 根据职位ID获取公司信息
     * GET /api/jobs/{id}/company
     * 
     * @param id 职位ID
     * @return 响应：
     * {
     *   "code": 200,
     *   "message": "获取职位所属公司信息成功",
     *   "data": {
     *     "id": 1,
     *     "name": "阿里巴巴",
     *     "description": "阿里巴巴集团是一家中国的电子商务公司，由马云创办...",
     *     "logo": "http://example.com/logo/alibaba.png",
     *     "city": ["杭州"],
     *     "size": "10000人以上",
     *     "industry": "互联网/电子商务",
     *     "financingStage": "已上市",
     *     "website": "https://www.alibaba.com",
     *     "address": "杭州市余杭区文一西路969号",
     *     "contactPerson": "HR招聘",
     *     "contactPosition": "招聘经理"
     *   }
     * }
     * 
     * 错误响应：
     * {
     *   "code": 500,
     *   "message": "获取职位所属公司信息失败: 职位不存在"
     * }
     */
} 