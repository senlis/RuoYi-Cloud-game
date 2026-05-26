import request from '@/utils/request'

export function listResource(query) {
  return request({ url: '/system/game/resource/list', method: 'get', params: query })
}
export function getResource(id) {
  return request({ url: '/system/game/resource/' + id, method: 'get' })
}
export function addResource(data) {
  return request({ url: '/system/game/resource', method: 'post', data })
}
export function updateResource(data) {
  return request({ url: '/system/game/resource', method: 'put', data })
}
export function delResource(id) {
  return request({ url: '/system/game/resource/' + id, method: 'delete' })
}
export function submitResource(id) {
  return request({ url: '/system/game/resource/submit/' + id, method: 'put' })
}
export function approveResource(id) {
  return request({ url: '/system/game/resource/audit/approve/' + id, method: 'put' })
}
export function rejectResource(id, remark) {
  return request({ url: '/system/game/resource/audit/reject/' + id, method: 'put', params: { remark } })
}
export function retryResource(id) {
  return request({ url: '/system/game/resource/retry/' + id, method: 'put' })
}
export function getApprovers(projectId) {
  return request({ url: '/system/game/resource/approvers', method: 'get', params: { projectId } })
}
