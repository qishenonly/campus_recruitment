import request from '../utils/request'

// 获取职位列表
export function getJobs(params) {
  return request({
    url: '/jobs',
    method: 'get',
    params
  })
}

// 获取职位详情
export function getJobDetail(id) {
  return request({
    url: `/jobs/${id}`,
    method: 'get'
  })
}

// 根据职位ID获取公司信息
export function getCompanyByJobId(id) {
  return request({
    url: `/jobs/${id}/company`,
    method: 'get'
  })
}

// 搜索职位
export function searchJobs(params) {
  return request({
    url: '/jobs/search',
    method: 'get',
    params
  })
}

// 获取企业职位列表
export function getCompanyJobs(companyId) {
  return request({
    url: `/jobs/company/${companyId}`,
    method: 'get'
  })
}

// 收藏职位
export function favoriteJob(jobId) {
  return request({
    url: `/jobs/${jobId}/favorite`,
    method: 'post'
  })
}

// 取消收藏
export function unfavoriteJob(jobId) {
  return request({
    url: `/jobs/${jobId}/favorite`, 
    method: 'delete'
  })
}

// 获取收藏状态
export function getFavoriteStatus(jobId) {
  return request({
    url: `/jobs/${jobId}/favorite`,
    method: 'get'
  })
}

// 获取收藏列表
export function getFavorites() {
  return request({
    url: '/jobs/favorites',
    method: 'get'
  })
}

// 投递职位
export function applyJob(jobId, resumeId, coverLetter) {
  return request({
    url: `/jobs/${jobId}/apply`,
    method: 'post',
    data: {
      resumeId,
      coverLetter
    }
  })
}

// 获取对话消息
export function getConversationMessages(conversationId) {
  return request({
    url: `/conversations/${conversationId}/messages`,
    method: 'get'
  })
}

// 获取投递记录
export function getApplications(params) {
  return request({
    url: '/jobs/applications',
    method: 'get',
    params: {
      ...params,
      includeJobInfo: true, // 包含职位信息
      includeCompanyInfo: true // 包含公司信息
    }
  })
}

// 撤回投递
export function withdrawApplication(applicationId) {
  return request({
    url: `/jobs/applications/${applicationId}/withdraw`,
    method: 'put'
  })
}

// 确保导出所有需要的函数
export default {
  getJobs,
  getJobDetail,
  getCompanyByJobId,
  searchJobs,
  getCompanyJobs,
  favoriteJob,
  unfavoriteJob,
  getFavoriteStatus,
  getFavorites,
  applyJob,
  getConversationMessages,
  getApplications,
  withdrawApplication
}