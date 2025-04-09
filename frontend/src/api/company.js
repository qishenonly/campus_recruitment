import request from '@/utils/request'

// 获取企业信息
export function getCompanyInfo() {
  return request({
    url: '/company/info',
    method: 'get'
  })
}

// 更新企业信息
export function updateCompanyInfo(data) {
  return request({
    url: '/company/info',
    method: 'put',
    data
  })
}

// 创建企业信息（首次完善信息使用）
export function createCompanyInfo(data) {
  return request({
    url: '/company/create',
    method: 'post',
    data
  })
}

// 上传企业Logo
export function uploadCompanyLogo(data) {
  return request({
    url: '/company/logo',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取企业团队成员列表
export function getTeamMembers() {
  return request({
    url: '/company/team',
    method: 'get'
  })
}

// 添加团队成员
export function addTeamMember(data) {
  return request({
    url: '/company/team',
    method: 'post',
    data
  })
}

// 更新团队成员信息
export function updateTeamMember(id, data) {
  return request({
    url: `/company/team/${id}`,
    method: 'put',
    data
  })
}

// 删除团队成员
export function deleteTeamMember(id) {
  return request({
    url: `/company/team/${id}`,
    method: 'delete'
  })
}

// 获取企业统计信息
export function getCompanyDashboard() {
  return request({
    url: '/company/employee/dashboard',
    method: 'get'
  })
}

export default {
  getCompanyInfo,
  updateCompanyInfo,
  createCompanyInfo,
  uploadCompanyLogo,
  getTeamMembers,
  addTeamMember,
  updateTeamMember,
  deleteTeamMember,
  getCompanyDashboard
} 