import request from '@/utils/request'

export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
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
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

export default {
  updateUserProfile,
  getUserProfile,
  getUserInfo
}