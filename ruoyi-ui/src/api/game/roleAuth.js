import request from '@/utils/request'

// 查询角色授权列表
export function listRoleAuth(query) {
  return request({
    url: '/system/game/roleAuth/list',
    method: 'get',
    params: query
  })
}

// 根据角色和实体类型获取授权的实体ID列表
export function getAuthByRole(roleId, entityType) {
  return request({
    url: '/system/game/roleAuth/byRole/' + roleId + '/' + entityType,
    method: 'get'
  })
}

// 保存角色授权
export function saveRoleAuth(data) {
  return request({
    url: '/system/game/roleAuth/save',
    method: 'post',
    data: data
  })
}

// 删除角色授权
export function delRoleAuth(authIds) {
  return request({
    url: '/system/game/roleAuth/' + authIds,
    method: 'delete'
  })
}
