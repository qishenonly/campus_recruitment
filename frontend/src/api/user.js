import request from '@/utils/request'

export function updateUserProfile(data) {
  return request({
    url: '/profile',
    method: 'post',
    data
  })
}

export function getUserProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
} 

export function getUserInfo(id) {
  // 特殊处理企业用户ID=2的情况，使用新的API接口
  const url = id === 2 ? `/company-users/${id}` : `/users/${id}`
  
  return request({
    url: url,
    method: 'get'
  })
}

export default {
  updateUserProfile,
  getUserProfile,
  getUserInfo
}