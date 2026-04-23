import axios from 'axios'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 网关统一前缀
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = userStore.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 如果返回的类型是 Blob 等直接返回
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return res
    }
    // 适配后端的 Result 格式 { success: true/false, data: ..., errorMsg: ... }
    if (res.success) {
      if (res.total !== undefined && res.total !== null) {
        return { records: res.data, total: res.total }
      }
      return res.data
    } else {
      ElMessage.error(res.errorMsg || '系统错误')
      if (res.errorMsg === '未登录' || res.errorMsg === '登录已过期') {
        const userStore = useUserStore()
        userStore.clearToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.errorMsg || 'Error'))
    }
  },
  error => {
    console.error('err' + error)
    let msg = '网络错误，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          msg = '登录已过期，请重新登录'
          const userStore = useUserStore()
          userStore.clearToken()
          router.push('/login')
          break
        case 403:
          msg = '没有权限访问'
          break
        case 404:
          msg = '请求的资源不存在'
          break
        case 500:
          msg = '服务器内部错误'
          break
      }
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
