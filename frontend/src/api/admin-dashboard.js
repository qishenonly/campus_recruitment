import request from '../utils/request'

/**
 * 获取基础统计数据
 * @returns {Promise}
 */
export function getBasicStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取用户注册趋势
 * @param {Object} params - 查询参数
 * @param {string} params.type - 统计类型：daily、weekly、monthly
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise}
 */
export function getUserRegisterTrend(params) {
  return request({
    url: '/admin/dashboard/register-trend',
    method: 'get',
    params
  })
}

/**
 * 获取职位统计
 * @returns {Promise}
 */
export function getJobStats() {
  return request({
    url: '/admin/dashboard/job-stats',
    method: 'get'
  })
}

/**
 * 获取简历投递统计
 * @param {Object} params - 查询参数
 * @param {string} params.type - 统计类型：daily、weekly、monthly
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise}
 */
export function getResumeSubmitStats(params) {
  return request({
    url: '/admin/dashboard/resume-stats',
    method: 'get',
    params
  })
}

/**
 * 获取热门职位类别
 * @param {number} [limit=10] - 获取数量
 * @returns {Promise}
 */
export function getHotJobCategories(limit = 10) {
  return request({
    url: '/admin/dashboard/hot-categories',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取热门学校排名
 * @param {number} [limit=10] - 获取数量
 * @returns {Promise}
 */
export function getTopSchools(limit = 10) {
  return request({
    url: '/admin/dashboard/top-schools',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取热门企业排名
 * @param {number} [limit=10] - 获取数量
 * @returns {Promise}
 */
export function getTopCompanies(limit = 10) {
  return request({
    url: '/admin/dashboard/top-companies',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取系统访问统计
 * @param {Object} params - 查询参数
 * @param {string} params.type - 统计类型：daily、weekly、monthly
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise}
 */
export function getVisitStats(params) {
  return request({
    url: '/admin/dashboard/visit-stats',
    method: 'get',
    params
  })
}

/**
 * 获取最近操作日志
 * @param {number} [limit=10] - 获取数量
 * @returns {Promise}
 */
export function getRecentLogs(limit = 10) {
  return request({
    url: '/admin/dashboard/recent-logs',
    method: 'get',
    params: { limit }
  })
} 