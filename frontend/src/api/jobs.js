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

// 确保导出所有需要的函数
export default {
  getJobs,
  getJobDetail,
  searchJobs,
  getCompanyJobs,
  favoriteJob,
  unfavoriteJob,
  getFavoriteStatus,
  getFavorites
}