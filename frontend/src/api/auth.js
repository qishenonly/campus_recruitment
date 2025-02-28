import request from '@/utils/request'

// 发送验证码
export function sendVerificationCode(email) {
  return request({
    url: '/auth/send-verification',
    method: 'post',
    data: { email }
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