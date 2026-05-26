package com.ruoyi.system.service;

import com.ruoyi.system.domain.GameServer;

import java.util.List;
import java.util.Map;

/**
 * 游戏服务器推送服务接口
 * <p>
 * 封装向游戏服务器推送消息的通用逻辑：
 * 1. 查询服务器的 backendUrl
 * 2. 获取项目的 md5key
 * 3. 计算 MD5 签名
 * 4. HTTP POST JSON 到指定路径
 * <p>
 * 适用于 GM 邮件、GM 道具发放、公告推送等需要与游戏服通信的场景。
 *
 * @author ruoyi
 */
public interface IServerPushService {

    /**
     * 向指定服务器推送消息
     *
     * @param projectId 项目ID（用于获取 md5key）
     * @param serverIds 目标服务器ID列表
     * @param apiPath   API 路径（如 "/mail"），将拼接到 backendUrl 后
     * @param params    请求参数（不含签名）
     * @return 每台服务器的推送结果 key=serverId, value=是否成功
     */
    Map<Integer, Boolean> pushToServers(Long projectId, List<Integer> serverIds, String apiPath, Map<String, String> params);

    /**
     * 向单台服务器推送消息
     *
     * @param projectId 项目ID
     * @param server    游戏服务器对象
     * @param apiPath   API 路径
     * @param params    请求参数
     * @return 是否成功
     */
    boolean pushToServer(Long projectId, GameServer server, String apiPath, Map<String, String> params);

    /**
     * 获取项目的 md5key
     *
     * @param projectId 项目ID
     * @return md5key 字符串，如果未配置则返回 null
     */
    String getMd5Key(Long projectId);
}
