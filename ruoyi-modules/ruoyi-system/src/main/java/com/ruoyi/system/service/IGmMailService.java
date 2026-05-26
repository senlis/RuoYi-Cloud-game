package com.ruoyi.system.service;

import com.ruoyi.system.domain.GmMail;

import java.util.List;

/**
 * GM 邮件Service接口
 *
 * @author ruoyi
 */
public interface IGmMailService {

    /**
     * 查询邮件列表
     */
    List<GmMail> selectGmMailList(GmMail mail);

    /**
     * 查询邮件详情
     */
    GmMail selectGmMailById(Long mailId);

    /**
     * 新增邮件（草稿状态）
     */
    int insertGmMail(GmMail mail);

    /**
     * 修改邮件
     */
    int updateGmMail(GmMail mail);

    /**
     * 删除邮件
     */
    int deleteGmMailById(Long mailId);

    /**
     * 提交审核（待审核 → 待审核（标记提交））
     */
    int submitForAudit(Long mailId);

    /**
     * 审核通过
     */
    int approveGmMail(Long mailId);

    /**
     * 审核驳回
     */
    int rejectGmMail(Long mailId, String remark);

    /**
     * 撤回邮件
     */
    int revokeGmMail(Long mailId);

    /**
     * 重推失败服务器
     */
    int retryGmMail(Long mailId);
}
