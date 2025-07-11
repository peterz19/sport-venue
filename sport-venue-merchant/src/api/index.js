import request from "@/utils/request"

// 商户认证API
export const authApi = {
  // 商户登录
  login(data) {
    return request({
      url: "/auth/merchant/login",
      method: "post",
      data
    })
  },

  // 商户登出
  logout() {
    return request({
      url: "/auth/merchant/logout",
      method: "post"
    })
  },

  // 获取商户信息
  getMerchantInfo() {
    return request({
      url: "/auth/merchant/info",
      method: "get"
    })
  }
}

// 商户场馆管理API
export const merchantVenueApi = {
  // 获取商户的场馆列表
  getVenueList(params) {
    return request({
      url: "/venues/merchant",
      method: "get",
      params
    })
  },

  // 获取场馆详情
  getVenueById(id) {
    return request({
      url: `/venues/${id}`,
      method: "get"
    })
  },

  // 更新场馆信息
  updateVenue(id, data) {
    return request({
      url: `/venues/${id}`,
      method: "put",
      data
    })
  },

  // 更新场馆状态
  updateVenueStatus(id, status) {
    return request({
      url: `/venues/${id}/status`,
      method: "put",
      params: { status }
    })
  },

  // 更新场馆使用人数
  updateVenueOccupancy(id, occupancy) {
    return request({
      url: `/venues/${id}/occupancy`,
      method: "put",
      params: { occupancy }
    })
  },

  // 更新场馆评分
  updateVenueRating(id, rating) {
    return request({
      url: `/venues/${id}/rating`,
      method: "put",
      params: { rating }
    })
  },

  // 获取场馆统计数据
  getVenueStats() {
    return request({
      url: "/venues/merchant/stats",
      method: "get"
    })
  }
}
