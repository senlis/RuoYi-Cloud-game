package com.ruoyi.system.service;

import com.ruoyi.system.domain.GmMail;

import java.util.List;

/**
 * GM 邮件发送Service接口
 * <p>
 * 审核通过后将邮件推送到各游戏服务器。
 *
 * @author ruoyi
 */
public interface IGmMailSendService {

    /**
     * 发送邮件到游戏服务器
     *
     * @param mail 邮件对象（含 serverIds、projectId 等完整信息）
     * @return 推送失败的服务区ID列表（空=全部成功）
     */
    List<Integer> sendMail(GmMail mail);
}
