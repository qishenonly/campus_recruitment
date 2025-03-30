import axios from 'axios'
import { showToast } from 'vant'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code && res.code !== 200) {
      showToast({
        message: res.message || '操作失败，请稍后重试',
        type: 'fail'
      })
      return Promise.reject(new Error(res.message || '操作失败，请稍后重试'))
    }
    return res.code ? res : { data: res }
  },
  error => {
    console.error('响应错误:', error)
    // 获取更具体的错误信息
    const errorMessage = error.response?.data?.message || 
                         error.response?.data?.error || 
                         error.message || 
                         '网络连接失败，请检查网络设置'
                         
    // 替换掉包含状态码的默认错误信息
    const finalMessage = errorMessage.includes('status code') ? 
                         '服务器暂时无法响应，请稍后再试' : 
                         errorMessage
    
    showToast({
      message: finalMessage,
      type: 'fail'
    })
    return Promise.reject(new Error(finalMessage))
  }
)

export default service