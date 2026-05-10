package com.ruoyi.auth.controller;

import com.ruoyi.auth.service.SecureKeyManageService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.annotation.InnerAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * SecureKey 管理控制器
 * <p>
 * 提供渠道 SecureKey 的生成、重置、查询接口。
 * SecureKey 用于游戏服上报数据时的签名认证，Gateway 通过 GserveAuthFilter 验证。
 * <p>
 * 安全规则：
 * - SecureKey 仅生成时展示一次，DB 存储 SHA-256 哈希
 * - 原始 Key 同时缓存到 Redis，供 Gateway 验签使用
 * - 重置后旧 Key 立即失效
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/securekey")
public class SecureKeyController {

    private static final Logger log = LoggerFactory.getLogger(SecureKeyController.class);

    @Autowired
    private  SecureKeyManageService secureKeyManageService;



    /**
     * 为渠道生成 SecureKey
     * <p>
     * 调用方需要 game:channel:securekey 权限
     *
     * @param channelId 渠道 ID
     * @return SecureKey（仅本次请求展示）
     */
    @PostMapping("/channel/{channelId}/generate")
    public R<Map<String, Object>> generateSecureKey(@PathVariable Long channelId) {
        if (channelId == null || channelId <= 0) {
            return R.fail("渠道 ID 无效");
        }

        try {
            String secureKey = secureKeyManageService.generateSecureKey(channelId);

            Map<String, Object> result = new HashMap<>();
            result.put("channelId", channelId);
            result.put("secureKey", secureKey);
            result.put("message", "SecureKey 生成成功，请立即保存。关闭后将无法再次查看原始 Key。");

            log.info("SecureKey 生成成功: channelId={}", channelId);
            return R.ok(result);
        } catch (ServiceException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("SecureKey 生成失败: channelId={}", channelId, e);
            return R.fail("SecureKey 生成失败: " + e.getMessage());
        }
    }

    /**
     * 重置渠道 SecureKey
     * <p>
     * 重置后旧 Key 立即失效，游戏服需要更新配置
     *
     * @param channelId 渠道 ID
     * @return 新的 SecureKey（仅本次请求展示）
     */
    @PutMapping("/channel/{channelId}/reset")
    public R<Map<String, Object>> resetSecureKey(@PathVariable Long channelId) {
        if (channelId == null || channelId <= 0) {
            return R.fail("渠道 ID 无效");
        }

        try {
            String secureKey = secureKeyManageService.resetSecureKey(channelId);

            Map<String, Object> result = new HashMap<>();
            result.put("channelId", channelId);
            result.put("secureKey", secureKey);
            result.put("message", "SecureKey 重置成功，旧 Key 已失效。请立即保存新 Key。");

            log.info("SecureKey 重置成功: channelId={}", channelId);
            return R.ok(result);
        } catch (ServiceException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("SecureKey 重置失败: channelId={}", channelId, e);
            return R.fail("SecureKey 重置失败: " + e.getMessage());
        }
    }

    /**
     * 查询渠道 SecureKey 信息（不返回原始 Key）
     *
     * @param channelId 渠道 ID
     * @return SecureKey 元信息
     */
    @GetMapping("/channel/{channelId}/info")
    public R<Map<String, Object>> getSecureKeyInfo(@PathVariable Long channelId) {
        if (channelId == null || channelId <= 0) {
            return R.fail("渠道 ID 无效");
        }

        try {
            Map<String, Object> info = secureKeyManageService.getSecureKeyInfo(channelId);
            return R.ok(info);
        } catch (ServiceException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 内部调用：获取渠道 SecureKey（供 Gateway 使用）
     * <p>
     * Gateway 在验签时通过此接口获取 SecureKey 作为兜底（优先从 Redis 读取）
     *
     * @param channelId 渠道 ID
     * @return SecureKey 原始值
     */
    @InnerAuth
    @GetMapping("/channel/{channelId}/key")
    public R<String> getSecureKey(@PathVariable Long channelId) {
        if (channelId == null || channelId <= 0) {
            return R.fail("渠道 ID 无效");
        }

        try {
            String secureKey = secureKeyManageService.getSecureKeyFromCache(channelId);
            if (StringUtils.isBlank(secureKey)) {
                return R.fail("SecureKey 未找到或已过期");
            }
            return R.ok(secureKey);
        } catch (Exception e) {
            return R.fail("获取 SecureKey 失败: " + e.getMessage());
        }
    }
}
