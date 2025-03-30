import request from '@/utils/request'

// 获取企业面试列表
export function getCompanyInterviews(params) {
  return request({
    url: '/company/interviews',
    method: 'get',
    params
  })
}

// 获取面试详情
export function getInterviewDetail(interviewId) {
  return request({
    url: `/company/interviews/${interviewId}`,
    method: 'get'
  })
}

// 创建面试
export function createInterview(data) {
  return request({
    url: '/company/interviews',
    method: 'post',
    data
  })
}

// 更新面试信息
export function updateInterview(interviewId, data) {
  return request({
    url: `/company/interviews/${interviewId}`,
    method: 'put',
    data
  })
}

// 更新面试状态
export function updateInterviewStatus(interviewId, status) {
  return request({
    url: `/company/interviews/${interviewId}/status`,
    method: 'put',
    data: { status }
  })
}

export default {
  getCompanyInterviews,
  getInterviewDetail,
  createInterview,
  updateInterview,
  updateInterviewStatus
} 