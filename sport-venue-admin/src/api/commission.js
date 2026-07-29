import request from '@/utils/request'

export const commissionApi = {
  summary() {
    return request({ url: '/admin/commissions/summary', method: 'get' })
  },
  merchantDetail(merchantId) {
    return request({ url: `/admin/commissions/merchants/${merchantId}`, method: 'get' })
  },
  getRule(merchantId) {
    return request({ url: `/admin/commissions/merchants/${merchantId}/rule`, method: 'get' })
  },
  updateRule(merchantId, data) {
    return request({ url: `/admin/commissions/merchants/${merchantId}/rule`, method: 'put', data })
  },
  settle(merchantId, data) {
    return request({ url: `/admin/commissions/merchants/${merchantId}/settle`, method: 'post', data })
  },
  settlements(merchantId) {
    return request({
      url: '/admin/commissions/settlements',
      method: 'get',
      params: merchantId ? { merchantId } : {}
    })
  },
  settlementDetail(id) {
    return request({ url: `/admin/commissions/settlements/${id}`, method: 'get' })
  }
}
