import axios from 'axios'
import { ElMessage } from 'element-plus'

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
    // 如果是下载文件类型，直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response.data
    }
    
    const res = response.data
    if (res.code && res.code !== 200) {
      // 处理特定的错误码
      if (res.code === 401) {
        ElMessage({
          message: '登录已过期，请重新登录',
          type: 'error',
          duration: 2000
        })
        localStorage.removeItem('token')
        setTimeout(() => {
          window.location.href = '/admin/login'
        }, 1500)
        return Promise.reject(new Error(res.message || '登录已过期'))
      }
      
      ElMessage({
        message: res.message || '操作失败，请稍后重试',
        type: 'error',
        duration: 2000
      })
      return Promise.reject(new Error(res.message || '操作失败，请稍后重试'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    // 检查是否是序列化错误（常见于循环引用）
    if (error.response?.data?.message && 
        (error.response.data.message.includes('Infinite recursion') ||
         error.response.data.message.includes('CircularReference') ||
         error.response.data.message.includes('StackOverflowError'))) {
      console.error('后端序列化错误:', error.response.data)
      ElMessage({
        message: '服务器数据处理错误，可能存在循环引用问题，请联系管理员',
        type: 'error',
        duration: 3000
      })
      // 返回空数据，避免前端崩溃
      return Promise.resolve({ data: [] })
    }
    
    // 获取更具体的错误信息
    const errorMessage = error.response?.data?.message || 
                         error.response?.data?.error || 
                         error.message || 
                         '网络连接失败，请检查网络设置'
                         
    // 替换掉包含状态码的默认错误信息
    const finalMessage = errorMessage.includes('status code') ? 
                         '服务器暂时无法响应，请稍后再试' : 
                         errorMessage
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      setTimeout(() => {
        window.location.href = '/admin/login'
      }, 1500)
    }
    
    ElMessage({
      message: finalMessage,
      type: 'error',
      duration: 2000
    })
    
    // 对于常见的服务器错误，返回空数据，让前端可以继续运行
    if (error.response && error.response.status >= 500) {
      console.warn('服务器错误，返回空数据')
      return Promise.resolve({ data: [] })
    }
    
    return Promise.reject(error)
  }
)

export default service