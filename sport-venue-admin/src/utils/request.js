import axios from "axios"
import { ElMessage } from "element-plus"
import router from "@/router"

// 创建axios实例
const request = axios.create({
  baseURL: "/api", // 通过vite代理到后端
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加token认证
    const token = localStorage.getItem("token")
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
    
    // 如果后端返回的是标准格式
    if (res.code !== undefined) {
      if (res.code === 200) {
        return res.data
      } else {
        ElMessage.error(res.message || "请求失败")
        return Promise.reject(new Error(res.message || "请求失败"))
      }
    }
    
    return res
  },
  error => {
    console.error("请求错误:", error)
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      ElMessage.error("登录已过期，请重新登录")
      // 清除本地存储
      localStorage.removeItem("token")
      localStorage.removeItem("userInfo")
      // 跳转到登录页
      router.push("/login")
      return Promise.reject(error)
    }
    
    // 处理403禁止访问错误
    if (error.response && error.response.status === 403) {
      ElMessage.error("没有权限访问该资源")
      return Promise.reject(error)
    }
    
    // 处理500服务器错误
    if (error.response && error.response.status === 500) {
      ElMessage.error("服务器内部错误")
      return Promise.reject(error)
    }
    
    // 对于404错误，显示错误消息
    if (error.response && error.response.status === 404) {
      ElMessage.error("请求的资源不存在")
      return Promise.reject(error)
    }
    
    ElMessage.error(error.message || "网络错误")
    return Promise.reject(error)
  }
)

export default request
