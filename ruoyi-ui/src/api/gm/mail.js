import request from '@/utils/request'

// 查询GM邮件列表
export function listGmMail(query) {
  return request({
    url: '/system/game/gm/mail/list',
    method: 'get',
    params: query
  })
}

// 查询GM邮件详细
export function getGmMail(mailId) {
  return request({
    url: '/system/game/gm/mail/' + mailId,
    method: 'get'
  })
}

// 新增GM邮件
export function addGmMail(data) {
  return request({
    url: '/system/game/gm/mail',
    method: 'post',
    data: data
  })
}

// 修改GM邮件
export function updateGmMail(data) {
  return request({
    url: '/system/game/gm/mail',
    method: 'put',
    data: data
  })
}

// 删除GM邮件
export function delGmMail(mailId) {
  return request({
    url: '/system/game/gm/mail/' + mailId,
    method: 'delete'
  })
}

// 提交审核
export function submitGmMail(mailId) {
  return request({
    url: '/system/game/gm/mail/submit/' + mailId,
    method: 'put'
  })
}

// ========== 审核接口 ==========

// 查询待审核列表
export function listAuditGmMail(query) {
  return request({
    url: '/system/game/gm/mail/audit/list',
    method: 'get',
    params: query
  })
}

// 审核通过
export function approveGmMail(mailId) {
  return request({
    url: '/system/game/gm/mail/audit/approve/' + mailId,
    method: 'put'
  })
}

// 审核驳回
export function rejectGmMail(mailId, remark) {
  return request({
    url: '/system/game/gm/mail/audit/reject/' + mailId,
    method: 'put',
    params: { remark }
  })
}

// 重推失败服务器
export function retryGmMail(mailId) {
  return request({
    url: '/system/game/gm/mail/retry/' + mailId,
    method: 'put'
  })
}
