import request from '@/utils/request'

// 查询渠道用户列表
export function listUser(query) {
  return request({
    url: '/system/game/bridge/user/list',
    method: 'get',
    params: query
  })
}

// 导出渠道用户列表
export function exportUser(query) {
  return request({
    url: '/system/game/bridge/user/export',
    method: 'post',
    params: query
  })
}
