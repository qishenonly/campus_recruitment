import request from '@/utils/request'

/**
 * 获取企业列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCompanyList(params) {
  console.log('API调用 - 获取企业列表，请求参数:', params)
  return request({
    url: '/admin/companies',
    method: 'get',
    params
  })
}

/**
 * 获取企业详情
 * @param {number} id 企业ID
 * @returns {Promise}
 */
export function getCompanyDetail(id) {
  console.log('API调用 - 获取企业详情，ID:', id)
  return request({
    url: `/admin/companies/${id}`,
    method: 'get'
  })
}

/**
 * 新增企业
 * @param {Object} data 企业数据
 * @returns {Promise}
 */
export function addCompany(data) {
  return request({
    url: '/admin/companies',
    method: 'post',
    data
  })
}

/**
 * 更新企业
 * @param {number} id 企业ID
 * @param {Object} data 企业数据
 * @returns {Promise}
 */
export function updateCompany(id, data) {
  return request({
    url: `/admin/companies/${id}`,
    method: 'put',
    data
  })
}

/**
 * 审核企业
 * @param {number} id 企业ID
 * @param {string} status 状态 ACTIVE/INACTIVE/BLOCKED
 * @returns {Promise}
 */
export function verifyCompany(id, status) {
  return request({
    url: `/admin/companies/${id}/verify`,
    method: 'post',
    data: { status }
  })
}

/**
 * 禁用企业
 * @param {number} id 企业ID
 * @returns {Promise}
 */
export function disableCompany(id) {
  return request({
    url: `/admin/companies/${id}/disable`,
    method: 'post'
  })
}

/**
 * 启用企业
 * @param {number} id 企业ID
 * @returns {Promise}
 */
export function enableCompany(id) {
  return request({
    url: `/admin/companies/${id}/enable`,
    method: 'post'
  })
}

/**
 * 删除企业
 * @param {number} id 企业ID
 * @returns {Promise}
 */
export function deleteCompany(id) {
  return request({
    url: `/admin/companies/${id}`,
    method: 'delete'
  })
}

/**
 * 获取企业岗位列表
 * @param {number} id 企业ID
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCompanyJobs(id, params) {
  return request({
    url: `/admin/companies/${id}/jobs`,
    method: 'get',
    params
  })
}

/**
 * 批量审核企业
 * @param {Array} ids 企业ID数组
 * @param {string} status 状态 ACTIVE/INACTIVE/BLOCKED
 * @returns {Promise}
 */
export function batchVerifyCompanies(ids, status) {
  return request({
    url: '/admin/companies/batch/verify',
    method: 'post',
    data: { ids, status }
  })
}

/**
 * 批量禁用企业
 * @param {Array} ids 企业ID数组
 * @returns {Promise}
 */
export function batchDisableCompanies(ids) {
  return request({
    url: '/admin/companies/batch/disable',
    method: 'post',
    data: { ids }
  })
}

/**
 * 批量启用企业
 * @param {Array} ids 企业ID数组
 * @returns {Promise}
 */
export function batchEnableCompanies(ids) {
  return request({
    url: '/admin/companies/batch/enable',
    method: 'post',
    data: { ids }
  })
}

/**
 * 批量删除企业
 * @param {Array} ids 企业ID数组
 * @returns {Promise}
 */
export function batchDeleteCompanies(ids) {
  return request({
    url: '/admin/companies/batch/delete',
    method: 'post',
    data: { ids }
  })
}

/**
 * 导出企业列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function exportCompanies(params) {
  return request({
    url: '/admin/companies/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
} 