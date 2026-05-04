import request from '@/utils/request'

// 查询动态字段定义列表
export function listFieldDefine(query) {
  return request({
    url: '/system/game/field/define/list',
    method: 'get',
    params: query
  })
}

// 查询动态字段定义详细
export function getFieldDefine(fieldDefineId) {
  return request({
    url: '/system/game/field/define/' + fieldDefineId,
    method: 'get'
  })
}

// 根据实体类型查询字段定义列表
export function listFieldByEntity(entityType) {
  return request({
    url: '/system/game/field/define/byEntity/' + entityType,
    method: 'get'
  })
}

// 新增动态字段定义
export function addFieldDefine(data) {
  return request({
    url: '/system/game/field/define',
    method: 'post',
    data: data
  })
}

// 修改动态字段定义
export function updateFieldDefine(data) {
  return request({
    url: '/system/game/field/define',
    method: 'put',
    data: data
  })
}

// 删除动态字段定义
export function delFieldDefine(fieldDefineId) {
  return request({
    url: '/system/game/field/define/' + fieldDefineId,
    method: 'delete'
  })
}
