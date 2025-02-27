import axios from 'axios'
import { showToast } from 'vant'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api', // 修改为相对路径
  timeout: 5000
})

// 响应拦截器
request.interceptors.response.use(
  response => response,
  error => {
    showToast(error.message)
    return Promise.reject(error)
  }
)

export default request  // 确保正确导出