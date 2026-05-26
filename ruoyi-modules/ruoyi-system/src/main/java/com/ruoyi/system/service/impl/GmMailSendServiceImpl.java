package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.GmMail;
import com.ruoyi.system.service.IGmMailSendService;
import com.ruoyi.system.service.IServerPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GM 邮件发送Service实现
 * <p>
 * 将审核通过的邮件推送到各游戏服务器，使用 {@link IServerPushService} 完成实际推送。
 *
 * @author ruoyi
 */
@Service
public class GmMailSendServiceImpl implements IGmMailSendService {

    private static final Logger log = LoggerFactory.getLogger(GmMailSendServiceImpl.class);

    @Autowired
    private IServerPushService serverPushService;

    @Override
    public List<Integer> sendMail(GmMail mail) {
        if (mail.getServerIds() == null || mail.getServerIds().isEmpty()) {
            log.warn("邮件 {} 未指定服务器，跳过发送", mail.getMailId());
            return Collections.emptyList();
        }

        List<Integer> serverIds = Arrays.stream(mail.getServerIds().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt)
                .collect(Collectors.toList());

        if (serverIds.isEmpty()) {
            log.warn("邮件 {} 服务器列表为空", mail.getMailId());
            return Collections.emptyList();
        }

        // 构建请求参数
        Map<String, String> params = buildParams(mail);

        // 推送并收集失败
        Map<Integer, Boolean> results = serverPushService.pushToServers(
                mail.getProjectId(), serverIds, "/mail", params);

        List<Integer> failedIds = new ArrayList<>();
        for (Map.Entry<Integer, Boolean> entry : results.entrySet()) {
            if (!entry.getValue()) {
                log.error("邮件 {} 推送到服务器 {} 失败", mail.getMailId(), entry.getKey());
                failedIds.add(entry.getKey());
            }
        }
        return failedIds;
    }

    /** 构建推送参数 */
    private Map<String, String> buildParams(GmMail mail) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mailId", String.valueOf(mail.getMailId()));
        params.put("title", mail.getTitle() != null ? mail.getTitle() : "");
        params.put("content", mail.getContent() != null ? mail.getContent() : "");
        params.put("targetType", String.valueOf(mail.getTargetType() != null ? mail.getTargetType() : 0));
        params.put("expireDays", String.valueOf(mail.getExpireDays() != null ? mail.getExpireDays() : 7));

        if (mail.getTargetType() != null && mail.getTargetType() == 1) {
            if (mail.getMinLevel() != null) params.put("minLevel", String.valueOf(mail.getMinLevel()));
            if (mail.getMaxLevel() != null) params.put("maxLevel", String.valueOf(mail.getMaxLevel()));
            if (mail.getMinVip() != null) params.put("minVip", String.valueOf(mail.getMinVip()));
            if (mail.getMaxVip() != null) params.put("maxVip", String.valueOf(mail.getMaxVip()));
        }

        if (mail.getTargetType() != null && mail.getTargetType() == 2 && mail.getTargetPlayers() != null) {
            params.put("targetPlayers", mail.getTargetPlayers());
        }

        if (mail.getRewards() != null && !mail.getRewards().isEmpty()) {
            params.put("rewards", mail.getRewards());
        }
        return params;
    }
}
