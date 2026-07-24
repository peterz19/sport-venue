import axios from "axios"
import { ElMessage } from "element-plus"

const request = axios.create({
  baseURL: "/api",
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const merchantInfo = JSON.parse(localStorage.getItem("merchantInfo") || "{}")
    if (merchantInfo.token) {
      config.headers.Authorization = `Bearer ${merchantInfo.token}`
    }
    if (merchantInfo.merchantId) {
      config.headers["X-Merchant-ID"] = merchantInfo.merchantId
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== undefined) {
      if (res.code === 200) {
        return res.data
      }
      ElMessage.error(res.message || "请求失败")
      return Promise.reject(new Error(res.message || "请求失败"))
    }
    return res
  },
  error => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message || "网络错误"
    if (status === 401 || status === 403) {
      ElMessage.error("登录已失效，请重新登录")
      localStorage.removeItem("merchantInfo")
      if (window.location.pathname !== "/login") {
        window.location.href = "/login"
      }
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
