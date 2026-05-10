import request from '@/utils/request'

// ==================== 事件类型配置 ====================

// 查询事件类型列表
export function listEventType(query) {
  return request({
    url: '/system/analytics/event-type/list',
    method: 'get',
    params: query
  })
}

// 查询事件类型详细
export function getEventType(id) {
  return request({
    url: '/system/analytics/event-type/' + id,
    method: 'get'
  })
}

// 新增事件类型
export function addEventType(data) {
  return request({
    url: '/system/analytics/event-type',
    method: 'post',
    data: data
  })
}

// 修改事件类型
export function updateEventType(data) {
  return request({
    url: '/system/analytics/event-type',
    method: 'put',
    data: data
  })
}

// 删除事件类型
export function delEventType(id) {
  return request({
    url: '/system/analytics/event-type/' + id,
    method: 'delete'
  })
}

// ==================== 统计查询 ====================

// 新增创角统计
export function getNewRoleDaily(params) {
  return request({
    url: '/system/analytics/new-role/daily',
    method: 'get',
    params: params
  })
}

// DAU 统计
export function getDauDaily(params) {
  return request({
    url: '/system/analytics/dau/daily',
    method: 'get',
    params: params
  })
}

// 留存率
export function getRetention(params) {
  return request({
    url: '/system/analytics/retention',
    method: 'get',
    params: params
  })
}

// 日流水
export function getRevenueDaily(params) {
  return request({
    url: '/system/analytics/revenue/daily',
    method: 'get',
    params: params
  })
}

// LTV
export function getLtv(params) {
  return request({
    url: '/system/analytics/ltv',
    method: 'get',
    params: params
  })
}

// 首充率
export function getFirstRechargeRate(params) {
  return request({
    url: '/system/analytics/first-recharge-rate',
    method: 'get',
    params: params
  })
}

// 等级分布
export function getLevelDistribution(params) {
  return request({
    url: '/system/analytics/level-distribution',
    method: 'get',
    params: params
  })
}

// 升级速度
export function getLevelupSpeed(params) {
  return request({
    url: '/system/analytics/levelup-speed',
    method: 'get',
    params: params
  })
}

// 事件统计
export function getEventSummary(params) {
  return request({
    url: '/system/analytics/event-summary',
    method: 'get',
    params: params
  })
}

// 玩家行为日志
export function getEventLog(params) {
  return request({
    url: '/system/analytics/event-log',
    method: 'get',
    params: params
  })
}

// 道具统计
export function getItemSummary(params) {
  return request({
    url: '/system/analytics/item-summary',
    method: 'get',
    params: params
  })
}

// 每日总览
export function getOverview(params) {
  return request({
    url: '/system/analytics/overview',
    method: 'get',
    params: params
  })
}

// 批量留存
export function getRetentionBatch(params) {
  return request({ url: '/system/analytics/retention-batch', method: 'get', params })
}

// 批量LTV
export function getLtvBatch(params) {
  return request({ url: '/system/analytics/ltv-batch', method: 'get', params })
}
