import request from '@/utils/request'

// 查询游戏渠道列表
export function listChannel(query) {
  return request({
    url: '/system/game/channel/list',
    method: 'get',
    params: query
  })
}

// 查询游戏渠道详细
export function getChannel(channelId) {
  return request({
    url: '/system/game/channel/' + channelId,
    method: 'get'
  })
}

// 查询指定项目的渠道列表
export function listChannelByProject(projectId) {
  return request({
    url: '/system/game/channel/listByProject/' + projectId,
    method: 'get'
  })
}

// 新增游戏渠道
export function addChannel(data) {
  return request({
    url: '/system/game/channel',
    method: 'post',
    data: data
  })
}

// 修改游戏渠道
export function updateChannel(data) {
  return request({
    url: '/system/game/channel',
    method: 'put',
    data: data
  })
}

// 删除游戏渠道
export function delChannel(channelId) {
  return request({
    url: '/system/game/channel/' + channelId,
    method: 'delete'
  })
}

// 获取 SecureKey 信息
export function getSecureKeyInfo(channelId) {
  return request({
    url: '/auth/securekey/channel/' + channelId + '/info',
    method: 'get'
  })
}

// 生成 SecureKey
export function generateSecureKey(channelId) {
  return request({
    url: '/auth/securekey/channel/' + channelId + '/generate',
    method: 'post'
  })
}

// 重置 SecureKey
export function resetSecureKey(channelId) {
  return request({
    url: '/auth/securekey/channel/' + channelId + '/reset',
    method: 'put'
  })
}
