package com.ruoyi.system.service;

import com.ruoyi.system.domain.GameResourceRequest;

import java.util.List;

/**
 * 内部资源申请Service接口
 *
 * @author ruoyi
 */
public interface IGameResourceRequestService {

    List<GameResourceRequest> selectList(GameResourceRequest req);
    GameResourceRequest selectById(Long requestId);
    int insert(GameResourceRequest req);
    int update(GameResourceRequest req);
    int deleteById(Long requestId);

    /** 提交审批 */
    int submitForAudit(Long requestId);
    /** 审批通过 */
    int approve(Long requestId);
    /** 审批驳回 */
    int reject(Long requestId, String remark);
    /** 撤回 */
    int revoke(Long requestId);
    /** 重推失败 */
    int retry(Long requestId);
}
