import request from "@/utils/request"

// 场馆管理API
export const venueApi = {
  // 获取场馆列表
  getVenueList(params) {
    return request({
      url: "/venues",
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

  // 创建场馆
  createVenue(data) {
    return request({
      url: "/venues",
      method: "post",
      data
    })
  },

  // 更新场馆
  updateVenue(id, data) {
    return request({
      url: `/venues/${id}`,
      method: "put",
      data
    })
  },

  // 删除场馆
  deleteVenue(id) {
    return request({
      url: `/venues/${id}`,
      method: "delete"
    })
  },

  // 根据商户ID查询场馆
  getVenuesByMerchant(merchantId) {
    return request({
      url: `/venues/merchant/${merchantId}`,
      method: "get"
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

  // 获取场馆类型列表
  getVenueTypes() {
    return request({
      url: "/venues/types",
      method: "get"
    })
  },

  // 获取场馆状态列表
  getVenueStatuses() {
    return request({
      url: "/venues/statuses",
      method: "get"
    })
  }
}
