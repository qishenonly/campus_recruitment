import request from '../utils/request'

/**
 * 获取学生列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.name] - 学生姓名
 * @param {string} [params.school] - 学校
 * @param {string} [params.major] - 专业
 * @param {string} [params.status] - 账号状态
 * @returns {Promise}
 */
export function getStudentList(params) {
  return request({
    url: '/admin/students',
    method: 'get',
    params
  })
}

/**
 * 获取学生详情
 * @param {string} id - 学生ID
 * @returns {Promise}
 */
export function getStudentDetail(id) {
  return request({
    url: `/admin/students/${id}`,
    method: 'get'
  })
}

/**
 * 更新学生信息
 * @param {string} id - 学生ID
 * @param {Object} data - 学生信息
 * @returns {Promise}
 */
export function updateStudent(id, data) {
  return request({
    url: `/admin/students/${id}`,
    method: 'put',
    data
  })
}

/**
 * 审核通过学生账号
 * @param {string} id - 学生ID
 * @returns {Promise}
 */
export function verifyStudent(id) {
  return request({
    url: `/admin/students/${id}/verify`,
    method: 'post'
  })
}

/**
 * 禁用学生账号
 * @param {string} id - 学生ID
 * @returns {Promise}
 */
export function disableStudent(id) {
  return request({
    url: `/admin/students/${id}/disable`,
    method: 'post'
  })
}

/**
 * 启用学生账号
 * @param {string} id - 学生ID
 * @returns {Promise}
 */
export function enableStudent(id) {
  return request({
    url: `/admin/students/${id}/enable`,
    method: 'post'
  })
}

/**
 * 删除学生账号
 * @param {string} id - 学生ID
 * @returns {Promise}
 */
export function deleteStudent(id) {
  return request({
    url: `/admin/students/${id}`,
    method: 'delete'
  })
}

/**
 * 重置学生密码
 * @param {string} id - 学生ID
 * @returns {Promise}
 */
export function resetStudentPassword(id) {
  return request({
    url: `/admin/students/${id}/reset-password`,
    method: 'post'
  })
}

/**
 * 批量审核学生
 * @param {Array} ids - 学生ID数组
 * @returns {Promise}
 */
export function batchVerifyStudents(ids) {
  return request({
    url: '/admin/students/batch-verify',
    method: 'post',
    data: { ids }
  })
}

/**
 * 批量禁用学生账号
 * @param {Array} ids - 学生ID数组
 * @returns {Promise}
 */
export function batchDisableStudents(ids) {
  return request({
    url: '/admin/students/batch-disable',
    method: 'post',
    data: { ids }
  })
}

/**
 * 批量启用学生账号
 * @param {Array} ids - 学生ID数组
 * @returns {Promise}
 */
export function batchEnableStudents(ids) {
  return request({
    url: '/admin/students/batch-enable',
    method: 'post',
    data: { ids }
  })
}

/**
 * 批量删除学生账号
 * @param {Array} ids - 学生ID数组
 * @returns {Promise}
 */
export function batchDeleteStudents(ids) {
  return request({
    url: '/admin/students/batch-delete',
    method: 'post',
    data: { ids }
  })
}

/**
 * 导出学生数据
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function exportStudents(params) {
  return request({
    url: '/admin/students/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 导入学生数据
 * @param {FormData} formData - 包含学生数据的表单
 * @returns {Promise}
 */
export function importStudents(formData) {
  return request({
    url: '/admin/students/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载学生导入模板
 * @returns {Promise}
 */
export function downloadStudentTemplate() {
  return request({
    url: '/admin/students/template',
    method: 'get',
    responseType: 'blob'
  })
} 