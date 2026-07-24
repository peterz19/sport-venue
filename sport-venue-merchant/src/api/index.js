import request from "@/utils/request"

export const authApi = {
  login(data) {
    return request({
      url: "/auth/merchant/login",
      method: "post",
      data
    })
  },

  logout() {
    return request({
      url: "/auth/logout",
      method: "post"
    })
  },

  getMerchantInfo() {
    return request({
      url: "/auth/user/info",
      method: "get"
    })
  }
}

export const merchantVenueApi = {
  getVenueList(params) {
    return request({
      url: "/business/venues/mine",
      method: "get",
      params
    })
  },

  getVenueById(id) {
    return request({
      url: `/venues/${id}`,
      method: "get"
    })
  },

  updateVenue(id, data) {
    return request({
      url: `/venues/${id}`,
      method: "put",
      data
    })
  },

  updateVenueStatus(id, status) {
    return request({
      url: `/venues/${id}/status`,
      method: "put",
      params: { status }
    })
  },

  updateVenueOccupancy(id, occupancy) {
    return request({
      url: `/venues/${id}/occupancy`,
      method: "put",
      params: { occupancy }
    })
  },

  getVenueStats() {
    return request({
      url: "/venues/statistics",
      method: "get"
    })
  }
}

export const productApi = {
  list(params) {
    return request({ url: "/business/products", method: "get", params })
  },
  cashierList(venueId) {
    return request({ url: "/business/products/cashier", method: "get", params: { venueId } })
  },
  categories() {
    return request({ url: "/business/products/categories", method: "get" })
  },
  create(data) {
    return request({ url: "/business/products", method: "post", data })
  },
  update(id, data) {
    return request({ url: `/business/products/${id}`, method: "put", data })
  },
  updateStatus(id, status) {
    return request({ url: `/business/products/${id}/status`, method: "put", data: { status } })
  },
  remove(id) {
    return request({ url: `/business/products/${id}`, method: "delete" })
  }
}

export const salesApi = {
  preview(data) {
    return request({ url: "/business/sales/preview", method: "post", data })
  },
  createOrder(data) {
    return request({ url: "/business/sales/orders", method: "post", data })
  },
  payCash(orderId, data = {}) {
    return request({ url: `/business/sales/orders/${orderId}/pay/cash`, method: "post", data })
  },
  cancelOrder(orderId, data = {}) {
    return request({ url: `/business/sales/orders/${orderId}/cancel`, method: "post", data })
  },
  getOrder(id) {
    return request({ url: `/business/sales/orders/${id}`, method: "get" })
  },
  getOrderStatus(id) {
    return request({ url: `/business/sales/orders/${id}/status`, method: "get" })
  },
  listOrders(params) {
    return request({ url: "/business/sales/orders", method: "get", params })
  },
  dailySummary(params) {
    return request({ url: "/business/sales/daily/summary", method: "get", params })
  },
  dailyProducts(params) {
    return request({ url: "/business/sales/daily/products", method: "get", params })
  },
  myVenues() {
    return request({ url: "/business/venues/mine", method: "get" })
  }
}
