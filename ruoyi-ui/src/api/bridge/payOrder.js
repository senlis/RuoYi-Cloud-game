import request from '@/utils/request'

// 查询支付订单列表
export function listPayOrder(query) {
  return request({
    url: '/system/game/bridge/payOrder/list',
    method: 'get',
    params: query
  })
}

// 导出支付订单列表
export function exportPayOrder(query) {
  return request({
    url: '/system/game/bridge/payOrder/export',
    method: 'post',
    params: query
  })
}
