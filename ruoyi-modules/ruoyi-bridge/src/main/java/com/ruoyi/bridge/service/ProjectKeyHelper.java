package com.ruoyi.bridge.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bridge.mapper.GameRefMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 项目密钥辅助工具
 * <p>
 * 从 channelKey 或 projectId 获取项目的 md5key（用于通信签名）。
 * 适用于认证响应签名、游戏服推送签名等场景。
 *
 * @author ruoyi
 */
@Component
public class ProjectKeyHelper {

    private static final Logger log = LoggerFactory.getLogger(ProjectKeyHelper.class);

    @Autowired
    private GameRefMapper gameRefMapper;

    /**
     * 根据渠道编码获取项目 md5key
     *
     * @param channelKey 渠道编码
     * @return md5key，未配置时返回空字符串
     */
    public String getMd5KeyByChannel(String channelKey) {
        try {
            String json = gameRefMapper.selectProjectDynamicFieldsByChannelCode(channelKey);
            return parseMd5Key(json);
        } catch (Exception e) {
            log.warn("获取渠道 {} md5key 失败: {}", channelKey, e.getMessage());
            return "";
        }
    }

    /**
     * 根据项目ID获取项目 md5key
     *
     * @param projectId 项目ID
     * @return md5key，未配置时返回空字符串
     */
    public String getMd5KeyByProject(Long projectId) {
        try {
            String json = gameRefMapper.selectProjectDynamicFields(projectId);
            return parseMd5Key(json);
        } catch (Exception e) {
            log.warn("获取项目 {} md5key 失败: {}", projectId, e.getMessage());
            return "";
        }
    }

    /** 从 dynamic_fields JSON 中解析 md5key */
    private String parseMd5Key(String json) {
        if (json == null || json.isBlank()) return "";
        JSONObject obj = JSON.parseObject(json);
        String key = obj.getString("md5key");
        return key != null ? key : "";
    }
}
