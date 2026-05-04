import request from '@/utils/request'

// 查询游戏服务器列表
export function listServer(query) {
  return request({
    url: '/system/game/server/list',
    method: 'get',
    params: query
  })
}

// 查询游戏服务器详细
export function getServer(id) {
  return request({
    url: '/system/game/server/' + id,
    method: 'get'
  })
}

// 查询指定分区的服务器列表
export function listServerByRegion(regionId) {
  return request({
    url: '/system/game/server/listByRegion/' + regionId,
    method: 'get'
  })
}

// 新增游戏服务器
export function addServer(data) {
  return request({
    url: '/system/game/server',
    method: 'post',
    data: data
  })
}

// 修改游戏服务器
export function updateServer(data) {
  return request({
    url: '/system/game/server',
    method: 'put',
    data: data
  })
}

// 删除游戏服务器
export function delServer(ids) {
  return request({
    url: '/system/game/server/' + ids,
    method: 'delete'
  })
}

// 刷新游戏数据源
export function refreshServerDs(regionId, serverId) {
  return request({
    url: '/system/game/server/refreshDs/' + regionId + '/' + serverId,
    method: 'post'
  })
}
