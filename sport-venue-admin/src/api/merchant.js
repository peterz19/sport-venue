import request from '@/utils/request'

export const merchantApi = {
  // 获取商户列表
  getMerchants() {
    return request({
      url: '/merchants',
      method: 'get'
    })
  },

  // 根据ID获取商户详情
  getMerchantById(id) {
    return request({
      url: `/merchants/${id}`,
      method: 'get'
    })
  }
} 