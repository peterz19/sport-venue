import request from '@/utils/request'

export const merchantApi = {
  getMerchants() {
    return request({ url: '/merchants', method: 'get' })
  },
  getMerchantById(id) {
    return request({ url: `/merchants/${id}`, method: 'get' })
  },
  overview(id) {
    return request({ url: `/merchants/${id}/overview`, method: 'get' })
  },
  onboard(data) {
    return request({ url: '/merchants/onboard', method: 'post', data })
  },
  update(id, data) {
    return request({ url: `/merchants/${id}`, method: 'put', data })
  },
  updateStatus(id, status) {
    return request({ url: `/merchants/${id}/status`, method: 'put', data: { status } })
  },
  getFeatures(id) {
    return request({ url: `/merchants/${id}/features`, method: 'get' })
  },
  updateFeatures(id, data) {
    return request({ url: `/merchants/${id}/features`, method: 'put', data })
  },
  listWxChannels(id) {
    return request({ url: `/merchants/${id}/wx-channels`, method: 'get' })
  },
  upsertWxChannel(id, data) {
    return request({ url: `/merchants/${id}/wx-channels`, method: 'put', data })
  },
  getWxPay(id) {
    return request({ url: `/merchants/${id}/wx-pay`, method: 'get' })
  },
  upsertWxPay(id, data) {
    return request({ url: `/merchants/${id}/wx-pay`, method: 'put', data })
  }
}
