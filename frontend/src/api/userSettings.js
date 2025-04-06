import request from '@/utils/request'

/**
 * 获取用户个人信息
 */
export function getUserProfile() {
  return request({
    url: '/user/settings/profile',
    method: 'get'
  })
}

/**
 * 更新用户个人信息
 * @param {Object} data 用户个人信息数据
 */
export function updateUserProfile(data) {
  return request({
    url: '/user/settings/profile',
    method: 'put',
    data
  })
}

/**
 * 上传用户头像
 * @param {FormData} formData 包含file字段的表单数据
 */
export function uploadUserAvatar(formData) {
  return request({
    url: '/user/settings/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改用户密码
 * @param {Object} data 密码数据 {oldPassword, newPassword, confirmPassword}
 */
export function changePassword(data) {
  return request({
    url: '/user/settings/password',
    method: 'post',
    data
  })
}

/**
 * 发送手机验证码
 * @param {string} phone 手机号
 */
export function sendPhoneCode(phone) {
  return request({
    url: '/user/settings/send-phone-code',
    method: 'post',
    params: { phone }
  })
}

/**
 * 绑定手机号
 * @param {string} phone 手机号
 * @param {string} code 验证码
 */
export function bindPhone(phone, code) {
  return request({
    url: '/user/settings/bind-phone',
    method: 'post',
    params: { phone, code }
  })
}

/**
 * 发送邮箱验证码
 * @param {string} email 邮箱
 */
export function sendEmailCode(email) {
  return request({
    url: '/user/settings/send-email-code',
    method: 'post',
    params: { email }
  })
}

/**
 * 绑定邮箱
 * @param {string} email 邮箱
 * @param {string} code 验证码
 */
export function bindEmail(email, code) {
  return request({
    url: '/user/settings/bind-email',
    method: 'post',
    params: { email, code }
  })
}

export default {
  getUserProfile,
  updateUserProfile,
  uploadUserAvatar,
  changePassword,
  sendPhoneCode,
  bindPhone,
  sendEmailCode,
  bindEmail
} 