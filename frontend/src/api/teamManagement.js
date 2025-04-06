import request from '@/utils/request'

/**
 * 获取团队成员列表
 * @param {Object} params 查询参数 {page, size}
 */
export function getTeamMembers(params) {
  return request({
    url: '/company/team',
    method: 'get',
    params
  })
}

/**
 * 添加团队成员
 * @param {Object} data 团队成员数据
 */
export function addTeamMember(data) {
  return request({
    url: '/company/team',
    method: 'post',
    data
  })
}

/**
 * 更新团队成员信息
 * @param {number} id 团队成员ID
 * @param {Object} data 团队成员数据
 */
export function updateTeamMember(id, data) {
  return request({
    url: `/company/team/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除团队成员
 * @param {number} id 团队成员ID
 */
export function deleteTeamMember(id) {
  return request({
    url: `/company/team/${id}`,
    method: 'delete'
  })
}

/**
 * 获取团队成员详情
 * @param {number} id 团队成员ID
 */
export function getTeamMember(id) {
  return request({
    url: `/company/team/${id}`,
    method: 'get'
  })
}

export default {
  getTeamMembers,
  addTeamMember,
  updateTeamMember,
  deleteTeamMember,
  getTeamMember
} 