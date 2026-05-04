import request from '@/utils/request'

// 查询游戏项目列表
export function listProject(query) {
  return request({
    url: '/system/game/project/list',
    method: 'get',
    params: query
  })
}

// 查询游戏项目详细
export function getProject(projectId) {
  return request({
    url: '/system/game/project/' + projectId,
    method: 'get'
  })
}

// 新增游戏项目
export function addProject(data) {
  return request({
    url: '/system/game/project',
    method: 'post',
    data: data
  })
}

// 修改游戏项目
export function updateProject(data) {
  return request({
    url: '/system/game/project',
    method: 'put',
    data: data
  })
}

// 删除游戏项目
export function delProject(projectId) {
  return request({
    url: '/system/game/project/' + projectId,
    method: 'delete'
  })
}
