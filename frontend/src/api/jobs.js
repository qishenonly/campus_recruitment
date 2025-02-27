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

// 确保导出所有需要的函数
export default {
  getJobs,
  getJobDetail,
  searchJobs
}