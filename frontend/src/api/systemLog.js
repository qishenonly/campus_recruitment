import request from '@/utils/request'

/**
 * 获取系统日志列表
 * @param {Object} params 查询参数
 * @returns {Promise} API响应
 */
export function getSystemLogs(params) {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}

/**
 * 获取日志详情
 * @param {Number} id 日志ID
 * @returns {Promise} API响应
 */
export function getSystemLogDetail(id) {
  return request({
    url: `/admin/logs/${id}`,
    method: 'get'
  })
}

/**
 * 获取最近的操作日志
 * @param {Number} limit 限制数量
 * @returns {Promise} API响应
 */
export function getRecentLogs(limit = 10) {
  return request({
    url: '/admin/logs/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取仪表盘最近的操作日志
 * @param {Number} limit 限制数量
 * @returns {Promise} API响应
 */
export function getDashboardRecentLogs(limit = 10) {
  return request({
    url: '/admin/dashboard/recent-logs',
    method: 'get',
    params: { limit }
  })
}

/**
 * 清理过期日志
 * @param {Number} days 保留天数
 * @returns {Promise} API响应
 */
export function cleanupLogs(days = 90) {
  return request({
    url: '/admin/logs/cleanup',
    method: 'delete',
    params: { days }
  })
}

export default {
  getSystemLogs,
  getSystemLogDetail,
  getRecentLogs,
  getDashboardRecentLogs,
  cleanupLogs
} 