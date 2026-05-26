package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.utils.SignUtils;
import com.ruoyi.system.domain.GameProject;
import com.ruoyi.system.domain.GameServer;
import com.ruoyi.system.mapper.GameServerMapper;
import com.ruoyi.system.service.IGameProjectService;
import com.ruoyi.system.service.IServerPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 游戏服务器推送服务实现
 * <p>
 * 可复用于 GM 邮件、道具发放、公告推送等需要与游戏服通信的场景。
 *
 * @author ruoyi
 */
@Service
public class ServerPushServiceImpl implements IServerPushService {

    private static final Logger log = LoggerFactory.getLogger(ServerPushServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GameServerMapper gameServerMapper;

    @Autowired
    private IGameProjectService gameProjectService;

    @Override
    public Map<Integer, Boolean> pushToServers(Long projectId, List<Integer> serverIds,
                                                String apiPath, Map<String, String> params) {
        Map<Integer, Boolean> results = new LinkedHashMap<>();
        if (serverIds == null || serverIds.isEmpty()) {
            return results;
        }

        List<GameServer> servers = gameServerMapper.selectGameServerByCodes(serverIds);

        for (GameServer server : servers) {
            boolean ok = pushToServer(projectId, server, apiPath, params);
            results.put(server.getServerId(), ok);
        }
        return results;
    }

    @Override
    public boolean pushToServer(Long projectId, GameServer server,
                                 String apiPath, Map<String, String> params) {
        String backendUrl = server.getBackendUrl();
        if (backendUrl == null || backendUrl.isEmpty()) {
            log.warn("服务器 {} ({}) 未配置 backendUrl", server.getServerId(), server.getServerName());
            return false;
        }

        // 添加服务端ID
        Map<String, String> signedParams = new LinkedHashMap<>(params != null ? params : new LinkedHashMap<>());
        signedParams.put("serverId", String.valueOf(server.getServerId()));

        // 计算 MD5 签名
        String md5Key = getMd5Key(projectId);
        String sign = SignUtils.md5Sign(signedParams, md5Key);
        signedParams.put("sign", sign);

        // REST POST
        return doPost(backendUrl + apiPath, signedParams);
    }

    @Override
    public String getMd5Key(Long projectId) {
        if (projectId == null) return null;
        try {
            GameProject project = gameProjectService.selectGameProjectById(projectId);
            if (project == null) return null;
            Object fieldsObj = project.getDynamicFieldsObj();
            if (fieldsObj instanceof JSONObject) {
                return ((JSONObject) fieldsObj).getString("md5key");
            }
            return null;
        } catch (Exception e) {
            log.error("获取项目 {} md5key 失败", projectId, e);
            return null;
        }
    }

    /** REST POST 发送 JSON */
    private boolean doPost(String urlStr, Map<String, String> params) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(params), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    urlStr, HttpMethod.POST, entity, String.class);

            log.debug("POST {} 响应: {}", urlStr, response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("POST {} 失败: {}", urlStr, e.getMessage());
            return false;
        }
    }
}
