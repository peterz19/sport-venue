import axios from "axios"
import { ElMessage } from "element-plus"

// 创建axios实例
const request = axios.create({
  baseURL: "/api", // 通过vite代理到后端
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加商户认证信息
    const merchantInfo = JSON.parse(localStorage.getItem("merchantInfo") || "{}")
    if (merchantInfo.merchantId) {
      config.headers["X-Merchant-ID"] = merchantInfo.merchantId
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
    ElMessage.error(error.message || "网络错误")
    return Promise.reject(error)
  }
)

export default request
