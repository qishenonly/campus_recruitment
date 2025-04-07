import request from '../utils/request'

/**
 * 管理员登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export function adminLogin(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

/**
 * 获取管理员信息
 * @returns {Promise}
 */
export function getAdminInfo() {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

/**
 * 更新管理员信息
 * @param {Object} data - 管理员信息
 * @returns {Promise}
 */
export function updateAdminInfo(data) {
  return request({
    url: '/admin/info',
    method: 'put',
    data
  })
}

/**
 * 管理员修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.currentPassword - 当前密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export function changeAdminPassword(data) {
  return request({
    url: '/admin/password',
    method: 'put',
    data
  })
}

/**
 * 管理员退出登录
 * @returns {Promise}
 */
export function adminLogout() {
  return request({
    url: '/admin/logout',
    method: 'post'
  })
} 