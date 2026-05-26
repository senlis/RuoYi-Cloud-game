import request from '@/utils/request'

// 首页统计数据
export function getDashboardStats() {
  return request({
    url: '/system/game/dashboard/stats',
    method: 'get'
  })
}

// 待办事项
export function getDashboardTodo() {
  return request({
    url: '/system/game/dashboard/todo',
    method: 'get'
  })
}
