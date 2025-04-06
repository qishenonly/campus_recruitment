import request from '@/utils/request'

// 获取企业发布的职位列表
export function getCompanyPublishedJobs(params) {
  return request({
    url: '/company/employee/jobs',
    method: 'get',
    params
  })
}

// 创建新职位
export function createJob(data) {
  return request({
    url: '/company/employee/jobs',
    method: 'post',
    data
  })
}

// 更新职位信息
export function updateJob(jobId, data) {
  return request({
    url: `/company/employee/jobs/${jobId}`,
    method: 'put',
    data
  })
}

// 删除职位
export function deleteJob(jobId) {
  return request({
    url: `/jobs/${jobId}`,
    method: 'delete'
  })
}

// 获取职位详情
export function getJobDetail(jobId) {
  return request({
    url: `/jobs/${jobId}`,
    method: 'get'
  })
}

// 更新职位状态（上线/下线）
export function updateJobStatus(jobId, status) {
  return request({
    url: `/company/employee/jobs/${jobId}/status`,
    method: 'put',
    data: { status }
  })
}

export default {
  getCompanyPublishedJobs,
  createJob,
  updateJob,
  deleteJob,
  getJobDetail,
  updateJobStatus
} 