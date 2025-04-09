import request from '@/utils/request'

// 发送验证码
export function sendVerificationCode(email) {
  return request({
    url: '/auth/send-verification',
    method: 'post',
    data: { email }
  })
}

// 验证验证码
export function verifyCode(email, code) {
  return request({
    url: '/auth/verify-code',
    method: 'post',
    data: { email, code }
  })
}

// 注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 重置密码
export function resetLoginPassword(data) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
} 