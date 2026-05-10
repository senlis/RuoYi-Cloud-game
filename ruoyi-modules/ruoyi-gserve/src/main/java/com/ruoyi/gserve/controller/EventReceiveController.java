package com.ruoyi.gserve.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.gserve.domain.dto.EventBatchDTO;
import com.ruoyi.gserve.service.EventReceiveService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 事件数据接收控制器
 * <p>
 * 接收游戏服通过 HTTP POST 上报的批量行为日志数据。
 * 认证由 Gateway 的 GserveAuthFilter（SecureKey 签名）完成。
 * <p>
 * 请求路径：POST /etl/receive
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/etl")
public class EventReceiveController {

    private static final Logger log = LoggerFactory.getLogger(EventReceiveController.class);

    private final EventReceiveService eventReceiveService;

    public EventReceiveController(EventReceiveService eventReceiveService) {
        this.eventReceiveService = eventReceiveService;
    }

    /**
     * 接收批量事件数据
     * <p>
     * 请求头（由 Gateway GserveAuthFilter 注入）：
     * - X-App-Id: 渠道 AppId
     * - X-Project-Id: 项目 ID（用于 ClickHouse 路由）
     * - X-ClickHouse-Config: 项目 ClickHouse 配置 JSON（由 Gateway 从 Redis 注入）
     */
    @PostMapping("/receive")
    public R<Map<String, Object>> receive(@Valid @RequestBody EventBatchDTO batch,
                                          HttpServletRequest request) {
        String appId = request.getHeader("X-App-Id");
        if (StringUtils.isBlank(appId)) {
            appId = batch.getAppId();
        }

        // 从请求头获取项目信息
        String projectIdStr = request.getHeader("X-Project-Id");
        Long projectId = null;
        if (StringUtils.isNotBlank(projectIdStr)) {
            try {
                projectId = Long.parseLong(projectIdStr);
            } catch (NumberFormatException e) {
                return R.fail("无效的 X-Project-Id: " + projectIdStr);
            }
        }

        // ClickHouse 配置 JSON（由 Gateway 从 Redis 读取后注入）
        String configJson = request.getHeader("X-ClickHouse-Config");

        if (projectId == null || StringUtils.isBlank(configJson)) {
            return R.fail("未指定项目 ID 或 ClickHouse 配置");
        }

        log.info("接收到事件上报: table={}, appId={}, projectId={}, events={}",
                batch.getTable(), appId, projectId,
                batch.getEvents() != null ? batch.getEvents().size() : 0);

        return eventReceiveService.receive(batch, appId, projectId, configJson);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public R<String> health() {
        return R.ok("gserve is running");
    }
}
