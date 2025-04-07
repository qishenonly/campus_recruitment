import request from '@/utils/request'

/**
 * 获取学生个人信息
 */
export function getStudentProfile() {
  return request({
    url: '/student/settings/profile',
    method: 'get'
  })
}

/**
 * 更新学生个人信息
 * @param {Object} data 学生个人信息数据
 */
export function updateStudentProfile(data) {
  return request({
    url: '/student/settings/profile',
    method: 'put',
    data
  })
}

/**
 * 上传学生头像
 * @param {FormData} formData 包含file字段的表单数据
 */
export function uploadStudentAvatar(formData) {
  return request({
    url: '/student/settings/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改密码
 * @param {Object} data 密码数据 {oldPassword, newPassword, confirmPassword}
 */
export function changeStudentPassword(data) {
  return request({
    url: '/student/settings/password',
    method: 'post',
    data
  })
}

/**
 * 发送手机验证码
 * @param {string} phone 手机号
 */
export function sendStudentPhoneCode(phone) {
  return request({
    url: '/student/settings/send-phone-code',
    method: 'post',
    params: { phone }
  })
}

/**
 * 绑定手机号
 * @param {string} phone 手机号
 * @param {string} code 验证码
 */
export function bindStudentPhone(phone, code) {
  return request({
    url: '/student/settings/bind-phone',
    method: 'post',
    params: { phone, code }
  })
}

/**
 * 发送邮箱验证码
 * @param {string} email 邮箱
 */
export function sendStudentEmailCode(email) {
  return request({
    url: '/student/settings/send-email-code',
    method: 'post',
    params: { email }
  })
}

/**
 * 绑定邮箱
 * @param {string} email 邮箱
 * @param {string} code 验证码
 */
export function bindStudentEmail(email, code) {
  return request({
    url: '/student/settings/bind-email',
    method: 'post',
    params: { email, code }
  })
}

export default {
  getStudentProfile,
  updateStudentProfile,
  uploadStudentAvatar,
  changeStudentPassword,
  sendStudentPhoneCode,
  bindStudentPhone,
  sendStudentEmailCode,
  bindStudentEmail
} 