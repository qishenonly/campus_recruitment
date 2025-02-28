import request from '../utils/request'

// 获取企业列表
export function getCompanies(params) {
  return request({
    url: '/companies',
    method: 'get',
    params
  })
}

// 搜索企业
export function searchCompanies(params) {
  return request({
    url: '/companies/search',
    method: 'get',
    params
  })
}

export default {
  getCompanies,
  searchCompanies
}