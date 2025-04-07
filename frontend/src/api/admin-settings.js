import request from '../utils/request'

/**
 * 获取系统基本设置
 * @returns {Promise}
 */
export function getBasicSettings() {
  return request({
    url: '/admin/settings/basic',
    method: 'get'
  })
}

/**
 * 更新系统基本设置
 * @param {Object} data - 系统基本设置
 * @returns {Promise}
 */
export function updateBasicSettings(data) {
  return request({
    url: '/admin/settings/basic',
    method: 'put',
    data
  })
}

/**
 * 上传系统LOGO
 * @param {FormData} formData - 包含LOGO图片的表单
 * @returns {Promise}
 */
export function uploadLogo(formData) {
  return request({
    url: '/admin/settings/logo',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取注册设置
 * @returns {Promise}
 */
export function getRegisterSettings() {
  return request({
    url: '/admin/settings/register',
    method: 'get'
  })
}

/**
 * 更新注册设置
 * @param {Object} data - 注册设置
 * @returns {Promise}
 */
export function updateRegisterSettings(data) {
  return request({
    url: '/admin/settings/register',
    method: 'put',
    data
  })
}

/**
 * 获取安全设置
 * @returns {Promise}
 */
export function getSecuritySettings() {
  return request({
    url: '/admin/settings/security',
    method: 'get'
  })
}

/**
 * 更新安全设置
 * @param {Object} data - 安全设置
 * @returns {Promise}
 */
export function updateSecuritySettings(data) {
  return request({
    url: '/admin/settings/security',
    method: 'put',
    data
  })
}

/**
 * 获取系统维护状态
 * @returns {Promise}
 */
export function getMaintenanceMode() {
  return request({
    url: '/admin/settings/maintenance',
    method: 'get'
  })
}

/**
 * 切换系统维护模式
 * @param {boolean} enable - 是否启用维护模式
 * @returns {Promise}
 */
export function toggleMaintenanceMode(enable) {
  return request({
    url: '/admin/settings/maintenance',
    method: 'put',
    data: { enable }
  })
}

/**
 * 备份系统数据
 * @returns {Promise}
 */
export function backupSystem() {
  return request({
    url: '/admin/settings/backup',
    method: 'post',
    responseType: 'blob'
  })
}

/**
 * 清理系统缓存
 * @returns {Promise}
 */
export function clearSystemCache() {
  return request({
    url: '/admin/settings/clear-cache',
    method: 'post'
  })
}

/**
 * 重置系统
 * @param {Object} data - 确认信息
 * @param {string} data.confirmation - 确认文本
 * @returns {Promise}
 */
export function resetSystem(data) {
  return request({
    url: '/admin/settings/reset',
    method: 'post',
    data
  })
}

/**
 * 获取所有系统设置
 * @returns {Promise}
 */
export function getAllSettings() {
  return request({
    url: '/admin/settings',
    method: 'get'
  })
}

/**
 * 更新所有系统设置
 * @param {Object} data - 所有系统设置
 * @returns {Promise}
 */
export function updateAllSettings(data) {
  return request({
    url: '/admin/settings',
    method: 'put',
    data
  })
} 