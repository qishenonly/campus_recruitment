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
        message: res.message || '请求失败',
        type: 'fail'
      })
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.code ? res : { data: res }
  },
  error => {
    console.error('响应错误:', error)
    showToast({
      message: error.response?.data?.message || error.message || '网络错误',
      type: 'fail'
    })
    return Promise.reject(error)
  }
)

export default service