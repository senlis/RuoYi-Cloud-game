import request from '@/utils/request'

// 查询渠道出包参数配置列表
export function listChannelArg(query) {
  return request({
    url: '/system/game/bridge/channelArg/list',
    method: 'get',
    params: query
  })
}

// 查询渠道出包参数配置详细
export function getChannelArg(channelId) {
  return request({
    url: '/system/game/bridge/channelArg/' + channelId,
    method: 'get'
  })
}

// 新增渠道出包参数配置
export function addChannelArg(data) {
  return request({
    url: '/system/game/bridge/channelArg',
    method: 'post',
    data: data
  })
}

// 修改渠道出包参数配置
export function updateChannelArg(data) {
  return request({
    url: '/system/game/bridge/channelArg',
    method: 'put',
    data: data
  })
}

// 删除渠道出包参数配置
export function delChannelArg(channelIds) {
  return request({
    url: '/system/game/bridge/channelArg/' + channelIds,
    method: 'delete'
  })
}

// 获取渠道下拉选项
export function listChannelOptions() {
  return request({
    url: '/system/game/bridge/channelArg/channelOptions',
    method: 'get'
  })
}

// 获取区域下拉选项
export function listRegionOptions(channelCode) {
  return request({
    url: '/system/game/bridge/channelArg/regionOptions/' + channelCode,
    method: 'get'
  })
}
