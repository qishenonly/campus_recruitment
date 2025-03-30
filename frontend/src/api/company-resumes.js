import request from '@/utils/request'

// 获取企业收到的简历列表
export function getCompanyResumes(params) {
  return request({
    url: '/company/resumes',
    method: 'get',
    params
  })
}

// 获取简历详情
export function getResumeDetail(resumeId) {
  return request({
    url: `/company/resumes/${resumeId}`,
    method: 'get'
  })
}

// 更新简历状态（已查看、已联系、已面试、已录用、已拒绝）
export function updateResumeStatus(resumeId, status) {
  return request({
    url: `/company/resumes/${resumeId}/status`,
    method: 'put',
    data: { status }
  })
}

// 下载简历
export function downloadResume(resumeId) {
  return request({
    url: `/company/resumes/${resumeId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

export default {
  getCompanyResumes,
  getResumeDetail,
  updateResumeStatus,
  downloadResume
} 