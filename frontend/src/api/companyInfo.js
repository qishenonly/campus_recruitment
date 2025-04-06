import request from '@/utils/request'

/**
 * 获取企业基本信息
 */
export function getCompanyInfo() {
  return request({
    url: '/company/info',
    method: 'get'
  })
}

/**
 * 更新企业基本信息
 * @param {Object} data 企业信息对象
 */
export function updateCompanyInfo(data) {
  return request({
    url: '/company/info',
    method: 'put',
    data
  })
}

/**
 * 上传企业Logo
 * @param {FormData} formData 包含file字段的表单数据
 */
export function uploadCompanyLogo(formData) {
  return request({
    url: '/company/logo',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default {
  getCompanyInfo,
  updateCompanyInfo,
  uploadCompanyLogo
} 