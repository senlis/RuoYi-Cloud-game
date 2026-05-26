package com.ruoyi.system.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.GmMail;
import com.ruoyi.system.helper.GameAuthContext;
import com.ruoyi.system.mapper.GmMailMapper;
import com.ruoyi.system.service.IGmMailService;
import com.ruoyi.system.service.IGmMailSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GM 邮件Service实现
 *
 * @author ruoyi
 */
@Service
public class GmMailServiceImpl implements IGmMailService {

    @Autowired
    private GmMailMapper gmMailMapper;

    @Autowired
    private IGmMailSendService gmMailSendService;

    @Autowired
    private GameAuthContext gameAuthContext;

    @Override
    public List<GmMail> selectGmMailList(GmMail mail) {
        // 非管理员用户，按 project_id 过滤
        if (!gameAuthContext.isAdmin()) {
            List<Long> authIds = gameAuthContext.getAuthProjectIds();
            if (authIds != null && !authIds.isEmpty()) {
                mail.getParams().put("authProjectIds", authIds);
            } else if (authIds != null && authIds.isEmpty()) {
                // 没有权限，返回空
                return List.of();
            }
        }
        return gmMailMapper.selectList(mail);
    }

    @Override
    public GmMail selectGmMailById(Long mailId) {
        return gmMailMapper.selectById(mailId);
    }

    @Override
    public int insertGmMail(GmMail mail) {
        mail.setStatus(0);
        return gmMailMapper.insert(mail);
    }

    @Override
    public int updateGmMail(GmMail mail) {
        GmMail existing = gmMailMapper.selectById(mail.getMailId());
        if (existing == null) throw new ServiceException("邮件不存在");
        if (existing.getStatus() != 0 && existing.getStatus() != 4)
            throw new ServiceException("仅待审核或驳回状态的邮件可以编辑");
        mail.setStatus(0);
        return gmMailMapper.update(mail);
    }

    @Override
    public int deleteGmMailById(Long mailId) {
        GmMail existing = gmMailMapper.selectById(mailId);
        if (existing == null) throw new ServiceException("邮件不存在");
        if (existing.getStatus() != 0 && existing.getStatus() != 4)
            throw new ServiceException("仅待审核或驳回状态的邮件可以删除");
        return gmMailMapper.deleteById(mailId);
    }

    @Override
    public int submitForAudit(Long mailId) {
        GmMail existing = gmMailMapper.selectById(mailId);
        if (existing == null) throw new ServiceException("邮件不存在");
        if (existing.getStatus() != 0)
            throw new ServiceException("仅待审核状态的邮件可以提交审核");
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveGmMail(Long mailId) {
        GmMail existing = gmMailMapper.selectById(mailId);
        if (existing == null) throw new ServiceException("邮件不存在");
        if (existing.getStatus() != 0)
            throw new ServiceException("仅待审核状态的邮件可以通过");

        // 立即发送：推送并记录失败
        if (existing.getSendType() != null && existing.getSendType() == 0) {
            List<Integer> failedIds = gmMailSendService.sendMail(existing);
            GmMail update = new GmMail();
            update.setMailId(mailId);
            update.setStatus(failedIds.isEmpty() ? 2 : 3);
            update.setFailedServerIds(failedIds.isEmpty() ? null
                    : failedIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            return gmMailMapper.update(update);
        }

        // 定时发送
        GmMail update = new GmMail();
        update.setMailId(mailId);
        update.setStatus(1);
        return gmMailMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectGmMail(Long mailId, String remark) {
        GmMail existing = gmMailMapper.selectById(mailId);
        if (existing == null) throw new ServiceException("邮件不存在");
        if (existing.getStatus() != 0)
            throw new ServiceException("仅待审核状态的邮件可以驳回");
        GmMail update = new GmMail();
        update.setMailId(mailId);
        update.setStatus(4);
        update.setAuditRemark(remark);
        return gmMailMapper.update(update);
    }

    @Override
    public int revokeGmMail(Long mailId) {
        GmMail existing = gmMailMapper.selectById(mailId);
        if (existing == null) throw new ServiceException("邮件不存在");
        if (existing.getStatus() != 0)
            throw new ServiceException("仅待审核状态的邮件可以撤回");
        GmMail update = new GmMail();
        update.setMailId(mailId);
        update.setStatus(5);
        return gmMailMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int retryGmMail(Long mailId) {
        GmMail existing = gmMailMapper.selectById(mailId);
        if (existing == null) throw new ServiceException("邮件不存在");
        if (existing.getFailedServerIds() == null || existing.getFailedServerIds().isEmpty())
            throw new ServiceException("没有需要重试的失败服务器");

        // 仅向失败服务器重推
        GmMail retryMail = new GmMail();
        retryMail.setMailId(mailId);
        retryMail.setProjectId(existing.getProjectId());
        retryMail.setServerIds(existing.getFailedServerIds());
        retryMail.setTitle(existing.getTitle());
        retryMail.setContent(existing.getContent());
        retryMail.setTargetType(existing.getTargetType());
        retryMail.setTargetPlayers(existing.getTargetPlayers());
        retryMail.setMinLevel(existing.getMinLevel());
        retryMail.setMaxLevel(existing.getMaxLevel());
        retryMail.setMinVip(existing.getMinVip());
        retryMail.setMaxVip(existing.getMaxVip());
        retryMail.setExpireDays(existing.getExpireDays());
        retryMail.setRewards(existing.getRewards());

        List<Integer> stillFailed = gmMailSendService.sendMail(retryMail);

        GmMail update = new GmMail();
        update.setMailId(mailId);
        update.setStatus(stillFailed.isEmpty() ? 2 : 3);
        update.setFailedServerIds(stillFailed.isEmpty() ? null
                : stillFailed.stream().map(String::valueOf).collect(Collectors.joining(",")));
        return gmMailMapper.update(update);
    }
}
