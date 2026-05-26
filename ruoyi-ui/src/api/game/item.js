import request from '@/utils/request'

// 查询道具列表
export function listItem(query) {
  return request({
    url: '/system/game/item/list',
    method: 'get',
    params: query
  })
}

// 导入道具Excel（指定项目）
export function importItem(projectId, file) {
  const formData = new FormData()
  formData.append('projectId', projectId)
  formData.append('file', file)
  return request({
    url: '/system/game/item/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除道具
export function delItem(projectId, itemId) {
  return request({
    url: '/system/game/item/' + projectId + '/' + itemId,
    method: 'delete'
  })
}

// 清空指定项目的所有道具
export function clearItem(projectId) {
  return request({
    url: '/system/game/item/clear/' + projectId,
    method: 'delete'
  })
}
