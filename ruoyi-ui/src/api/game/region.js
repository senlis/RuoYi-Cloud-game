import request from '@/utils/request'

// 查询游戏分区列表
export function listRegion(query) {
  return request({
    url: '/system/game/region/list',
    method: 'get',
    params: query
  })
}

// 查询游戏分区详细
export function getRegion(regionId) {
  return request({
    url: '/system/game/region/' + regionId,
    method: 'get'
  })
}

// 查询指定渠道的分区列表
export function listRegionByChannel(channelId) {
  return request({
    url: '/system/game/region/listByChannel/' + channelId,
    method: 'get'
  })
}

// 新增游戏分区
export function addRegion(data) {
  return request({
    url: '/system/game/region',
    method: 'post',
    data: data
  })
}

// 修改游戏分区
export function updateRegion(data) {
  return request({
    url: '/system/game/region',
    method: 'put',
    data: data
  })
}

// 删除游戏分区
export function delRegion(regionId) {
  return request({
    url: '/system/game/region/' + regionId,
    method: 'delete'
  })
}

// 导出分区配置
export function exportConfig(regionId) {
  return request({
    url: '/system/game/region/' + regionId + '/exportConfig',
    method: 'post'
  })
}

// 获取分区config（region级别导出配置）
export function getRegionConfig(regionId) {
  return request({
    url: '/system/game/region/' + regionId + '/config',
    method: 'get'
  })
}

// 获取分区servers（服务器列表导出配置）
export function getRegionServers(regionId) {
  return request({
    url: '/system/game/region/' + regionId + '/servers',
    method: 'get'
  })
}
