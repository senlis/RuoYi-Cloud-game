package com.ruoyi.gateway.filter;

import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.gateway.config.properties.IgnoreWhiteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * gserve 请求认证过滤器 — SecureKey 签名验证
 * <p>
 * 拦截 /gserve/** 路径的请求，使用 SecureKey 签名认证机制。
 * 不走 JWT 登录认证，而是通过渠道配置的 SecureKey 验证请求签名。
 * <p>
 * 认证流程：
 * 1. 从请求头提取 X-App-Id, X-Timestamp, X-Signature, X-Sign-Type
 * 2. 校验时间戳是否在允许窗口内（±5 分钟，防重放）
 * 3. 根据 appId 从 Redis 获取渠道 SecureKey
 * 4. 拼接签名原文（method + path + timestamp + bodyMd5），计算预期签名
 * 5. 对比签名，通过后注入 X-App-Id / X-Project-Id 到下游请求头
 * <p>
 * 过滤器顺序：-100（在 AuthFilter order=-200 之后执行）
 *
 * @author ruoyi
 */
@Component
public class GserveAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GserveAuthFilter.class);

    /** SecureKey Redis 缓存前缀 */
    private static final String SECURE_KEY_CACHE_PREFIX = CacheConstants.SYS_CONFIG_KEY + "channel:securekey:";

    /** 时间戳允许窗口（秒） */
    private static final long TIMESTAMP_WINDOW = 300;

    private final RedisService redisService;
    private final IgnoreWhiteProperties ignoreWhite;

    public GserveAuthFilter(RedisService redisService, IgnoreWhiteProperties ignoreWhite) {
        this.redisService = redisService;
        this.ignoreWhite = ignoreWhite;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 只处理 /gserve/ 路径
        if (!path.startsWith("/gserve/")) {
            return chain.filter(exchange);
        }

        // 检查白名单（gserve 路径也有少量白名单，如健康检查）
        if (StringUtils.matches(path, ignoreWhite.getWhites())) {
            return chain.filter(exchange);
        }

        // 1. 提取签名头
        String appId = exchange.getRequest().getHeaders().getFirst("X-App-Id");
        String timestamp = exchange.getRequest().getHeaders().getFirst("X-Timestamp");
        String signature = exchange.getRequest().getHeaders().getFirst("X-Signature");
        String signType = exchange.getRequest().getHeaders().getFirst("X-Sign-Type");

        if (StringUtils.isBlank(appId)) {
            return unauthorized(exchange, "缺少 X-App-Id");
        }
        if (StringUtils.isBlank(timestamp)) {
            return unauthorized(exchange, "缺少 X-Timestamp");
        }
        if (StringUtils.isBlank(signature)) {
            return unauthorized(exchange, "缺少 X-Signature");
        }

        // 2. 校验时间戳（防重放攻击）
        try {
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - ts) > TIMESTAMP_WINDOW) {
                return unauthorized(exchange, "请求时间戳已过期（允许 " + TIMESTAMP_WINDOW + " 秒窗口）");
            }
        } catch (NumberFormatException e) {
            return unauthorized(exchange, "X-Timestamp 格式无效");
        }

        // 3. 从 Redis 获取 SecureKey
        String secureKey = redisService.getCacheObject(SECURE_KEY_CACHE_PREFIX + appId);
        if (StringUtils.isBlank(secureKey)) {
            log.warn("SecureKey 未找到: appId={}", appId);
            return unauthorized(exchange, "无效的 AppId 或 SecureKey 未配置");
        }

        // 4. 验证签名
        try {
            String method = exchange.getRequest().getMethod().name();
            String bodyMd5 = computeBodyMd5(exchange);
            String signContent = buildSignContent(method, path, timestamp, bodyMd5);
            String expectedSign = computeSignature(signContent, secureKey, signType);

            if (!MessageDigest.isEqual(expectedSign.getBytes(), signature.getBytes())) {
                log.warn("签名验证失败: appId={}, path={}", appId, path);
                return unauthorized(exchange, "签名验证失败");
            }
        } catch (Exception e) {
            log.error("签名验证异常: appId={}", appId, e);
            return unauthorized(exchange, "签名验证异常");
        }

        // 5. 注入下游请求头（gserve 需要这些信息来路由 ClickHouse）
        //    从 Redis 查询 channel→project 映射和 clickhouse_config
        String projectId = redisService.getCacheObject(SECURE_KEY_CACHE_PREFIX + appId + ":projectId");
        String clickhouseConfig = redisService.getCacheObject(SECURE_KEY_CACHE_PREFIX + appId + ":chConfig");

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> {
                    r.header("X-App-Id", appId);
                    if (StringUtils.isNotBlank(projectId)) {
                        r.header("X-Project-Id", projectId);
                    }
                    if (StringUtils.isNotBlank(clickhouseConfig)) {
                        r.header("X-ClickHouse-Config", clickhouseConfig);
                    }
                })
                .build();

        log.debug("gserve 请求认证通过: appId={}, projectId={}, path={}",
                appId, projectId, path);
        return chain.filter(mutated);
    }

    /**
     * 构建签名原文
     * method + "\n" + path + "\n" + timestamp + "\n" + bodyMd5
     */
    private String buildSignContent(String method, String path, String timestamp, String bodyMd5) {
        return method + "\n" + path + "\n" + timestamp + "\n" + (bodyMd5 != null ? bodyMd5 : "");
    }

    /**
     * 计算请求体 MD5
     */
    private String computeBodyMd5(ServerWebExchange exchange) {
        // 由于 WebFlux 的请求体只能读取一次，此处采用简化方案：
        // 从请求头 X-Body-Md5 读取（由游戏服在签名时计算并额外提供）
        String bodyMd5 = exchange.getRequest().getHeaders().getFirst("X-Body-Md5");
        if (bodyMd5 != null) {
            return bodyMd5;
        }
        // 兜底：GET 请求无 body，使用空字符串 MD5
        if ("GET".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return "d41d8cd98f00b204e9800998ecf8427e"; // MD5("")
        }
        // POST 请求需要读取 body，但由于 WebFlux body 只能消费一次，
        // 完整的实现需要在过滤器早期缓存 body（此处标记为 TODO）
        // TODO: 实现 WebFlux 环境下的请求体缓存读取
        return "";
    }

    /**
     * 计算签名
     */
    private String computeSignature(String data, String key, String signType) throws Exception {
        if ("hmac-sha256".equalsIgnoreCase(signType)) {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            return bytesToHex(mac.doFinal(data.getBytes()));
        } else {
            // 默认 MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes());
            md.update(key.getBytes());
            return bytesToHex(md.digest());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        log.warn("gserve 认证拒绝: {}", msg);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
