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
