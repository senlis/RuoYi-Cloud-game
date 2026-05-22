import request from '@/utils/request'

// 查询渠道接入平台配置列表
export function listPlatform(query) {
  return request({
    url: '/system/game/bridge/platform/list',
    method: 'get',
    params: query
  })
}

// 查询渠道接入平台配置详细
export function getPlatform(platformId) {
  return request({
    url: '/system/game/bridge/platform/' + platformId,
    method: 'get'
  })
}

// 新增渠道接入平台配置
export function addPlatform(data) {
  return request({
    url: '/system/game/bridge/platform',
    method: 'post',
    data: data
  })
}

// 修改渠道接入平台配置
export function updatePlatform(data) {
  return request({
    url: '/system/game/bridge/platform',
    method: 'put',
    data: data
  })
}

// 删除渠道接入平台配置
export function delPlatform(platformIds) {
  return request({
    url: '/system/game/bridge/platform/' + platformIds,
    method: 'delete'
  })
}

// 测试数据库连接
export function testDbConnection(data) {
  return request({
    url: '/system/game/bridge/platform/testConnection',
    method: 'post',
    data: data
  })
}
