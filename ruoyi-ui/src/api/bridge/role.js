import request from '@/utils/request'

// 查询角色信息列表
export function listRole(query) {
  return request({
    url: '/system/game/bridge/role/list',
    method: 'get',
    params: query
  })
}

// 导出角色信息列表
export function exportRole(query) {
  return request({
    url: '/system/game/bridge/role/export',
    method: 'post',
    params: query
  })
}
