package com.ruoyi.auth.service;

import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.system.api.RemoteGameChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SecureKey 管理服务
 * <p>
 * 通过 Feign 远程调用 system 模块查询/更新渠道配置，
 * 自己处理 SecureKey 的随机生成和哈希存储。
 *
 * @author ruoyi
 */
@Service
public class SecureKeyManageService {

    private static final Logger log = LoggerFactory.getLogger(SecureKeyManageService.class);

    /**
     * Redis 缓存前缀
     */
    private static final String REDIS_KEY_PREFIX = CacheConstants.SYS_CONFIG_KEY + "channel:securekey:";

    /**
     * Redis 缓存 TTL（7 天）
     */
    private static final long REDIS_TTL_SECONDS = 7 * 24 * 3600;

    @Autowired
    private RemoteGameChannelService remoteGameChannelService;

    @Autowired
    private RedisService redisService;

    /**
     * 生成 SecureKey
     *
     * @param channelId 渠道 ID
     * @return 原始 SecureKey（仅本次返回）
     */
    public String generateSecureKey(Long channelId) {
        // 1. 通过 Feign 查询渠道信息
        R<Map<String, Object>> result = remoteGameChannelService.getChannelSecureInfo(
                channelId, SecurityConstants.INNER);
        if (R.FAIL == result.getCode() || result.getData() == null) {
            throw new ServiceException("渠道不存在: " + channelId);
        }

        Map<String, Object> info = result.getData();
        String appId = (String) info.get("channelCode");
        Long projectId = longValue(info.get("projectId"));
        String clickhouseConfig = (String) info.get("clickhouseConfig");

        // 2. 生成随机 SecureKey（128 位 = 16 字节，hex 编码 = 32 字符）
        String secureKey = generateRandomKey();
        String salt = generateRandomKey();

        // 3. 计算哈希
        String hash = sha256(secureKey + salt);
        Integer newVersion = intValue(info.get("secureKeyVersion")) + 1;

        // 4. 通过 Feign 更新数据库
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("secureKeyHash", hash);
        updateData.put("secureKeySalt", salt);
        updateData.put("secureKeyVersion", newVersion);
        updateData.put("secureKeyUpdatedAt", new Date());
        remoteGameChannelService.updateChannelSecureKey(
                channelId, updateData, SecurityConstants.INNER);

        // 5. 缓存到 Redis（Gateway 验签 + 项目路由信息）
        redisService.setCacheObject(REDIS_KEY_PREFIX + appId, secureKey, REDIS_TTL_SECONDS, TimeUnit.SECONDS);
        cacheProjectInfo(appId, projectId, clickhouseConfig);

        log.info("SecureKey 已生成: channelId={}, appId={}, version={}", channelId, appId, newVersion);
        return secureKey;
    }

    /**
     * 重置 SecureKey
     */
    public String resetSecureKey(Long channelId) {
        R<Map<String, Object>> result = remoteGameChannelService.getChannelSecureInfo(
                channelId, SecurityConstants.INNER);
        if (R.FAIL == result.getCode() || result.getData() == null) {
            throw new ServiceException("渠道不存在: " + channelId);
        }

        Map<String, Object> info = result.getData();
        String appId = (String) info.get("channelCode");
        Long projectId = longValue(info.get("projectId"));
        String clickhouseConfig = (String) info.get("clickhouseConfig");

        // 删除旧 Redis 缓存
        redisService.deleteObject(REDIS_KEY_PREFIX + appId);

        // 生成新 Key
        String secureKey = generateRandomKey();
        String salt = generateRandomKey();
        String hash = sha256(secureKey + salt);
        Integer newVersion = intValue(info.get("secureKeyVersion")) + 1;

        // 更新数据库
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("secureKeyHash", hash);
        updateData.put("secureKeySalt", salt);
        updateData.put("secureKeyVersion", newVersion);
        updateData.put("secureKeyUpdatedAt", new Date());
        remoteGameChannelService.updateChannelSecureKey(
                channelId, updateData, SecurityConstants.INNER);

        // 缓存新 Key
        redisService.setCacheObject(REDIS_KEY_PREFIX + appId, secureKey, REDIS_TTL_SECONDS, TimeUnit.SECONDS);
        cacheProjectInfo(appId, projectId, clickhouseConfig);

        log.info("SecureKey 已重置: channelId={}, appId={}, version={}", channelId, appId, newVersion);
        return secureKey;
    }

    /**
     * 获取 SecureKey 信息（不含原始 Key）
     */
    public Map<String, Object> getSecureKeyInfo(Long channelId) {
        R<Map<String, Object>> result = remoteGameChannelService.getChannelSecureInfo(
                channelId, SecurityConstants.INNER);
        if (R.FAIL == result.getCode() || result.getData() == null) {
            throw new ServiceException("渠道不存在: " + channelId);
        }

        Map<String, Object> info = result.getData();
        String appId = (String) info.get("channelCode");
        boolean hasKey = StringUtils.isNotBlank((String) info.get("secureKeyHash"));
        boolean cached = redisService.hasKey(REDIS_KEY_PREFIX + appId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("channelId", channelId);
        resp.put("appId", appId);
        resp.put("secureKeyConfigured", hasKey);
        resp.put("secureKeyVersion", info.get("secureKeyVersion"));
        resp.put("secureKeyUpdatedAt", info.get("secureKeyUpdatedAt"));
        resp.put("secureKeyCached", cached);
        resp.put("message", hasKey
                ? "SecureKey 已配置。如需查看原始 Key，请重置或重新生成。"
                : "SecureKey 未配置，请先生成。");
        return resp;
    }

    /**
     * 从缓存获取 SecureKey 原始值
     */
    public String getSecureKeyFromCache(Long channelId) {
        R<Map<String, Object>> result = remoteGameChannelService.getChannelSecureInfo(
                channelId, SecurityConstants.INNER);
        if (R.FAIL == result.getCode() || result.getData() == null) {
            return null;
        }

        String appId = (String) result.getData().get("channelCode");
        String key = redisService.getCacheObject(REDIS_KEY_PREFIX + appId);
        if (key == null && StringUtils.isNotBlank((String) result.getData().get("secureKeyHash"))) {
            log.warn("SecureKey 缓存已过期: channelId={}", channelId);
            return null;
        }
        return key;
    }

    /**
     * 缓存项目信息到 Redis（Gateway 注入请求头时使用）
     */
    private void cacheProjectInfo(String appId, Long projectId, String clickhouseConfig) {
        if (projectId == null) return;

        redisService.setCacheObject(REDIS_KEY_PREFIX + appId + ":projectId",
                String.valueOf(projectId), REDIS_TTL_SECONDS, TimeUnit.SECONDS);

        if (StringUtils.isNotBlank(clickhouseConfig)) {
            redisService.setCacheObject(REDIS_KEY_PREFIX + appId + ":chConfig",
                    clickhouseConfig, REDIS_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[16];
        random.nextBytes(key);
        return bytesToHex(key);
    }

    private String sha256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytesToHex(md.digest(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 计算失败", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private long longValue(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(String.valueOf(obj));
    }

    private int intValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(String.valueOf(obj));
    }
}
