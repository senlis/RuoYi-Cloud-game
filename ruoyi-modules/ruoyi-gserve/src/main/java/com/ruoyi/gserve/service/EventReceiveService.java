package com.ruoyi.gserve.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.gserve.config.ClickHouseConfig;
import com.ruoyi.gserve.domain.dto.EventBatchDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 事件接收服务
 * <p>
 * 负责校验游戏服上报的事件数据，解析并分发到对应的 ClickHouse 写入服务。
 *
 * @author ruoyi
 */
@Service
public class EventReceiveService {

    private static final Logger log = LoggerFactory.getLogger(EventReceiveService.class);

    /** 允许写入的表名白名单 */
    private static final Set<String> ALLOWED_TABLES = Set.of(
            "game_event_chara_creation",
            "game_event_role_login",
            "game_event_role_levelup",
            "game_event_recharge",
            "game_event_role_event",
            "game_event_item_change"
    );

    private final ClickHouseWriterService clickHouseWriterService;

    public EventReceiveService(ClickHouseWriterService clickHouseWriterService) {
        this.clickHouseWriterService = clickHouseWriterService;
    }

    /**
     * 接收并处理批量事件
     *
     * @param batch      事件批量数据
     * @param appId      渠道 AppId（由 Gateway 注入）
     * @param projectId  项目 ID（用于 ClickHouse 路由）
     * @param configJson 项目 ClickHouse 配置 JSON
     * @return 处理结果
     */
    public R<Map<String, Object>> receive(EventBatchDTO batch, String appId,
                                          Long projectId, String configJson) {
        // 1. 校验表名
        String tableName = batch.getTable();
        if (!ALLOWED_TABLES.contains(tableName)) {
            return R.fail("不允许写入的表: " + tableName);
        }

        // 2. 校验事件数据
        List<EventBatchDTO.EventEntry> events = batch.getEvents();
        if (events == null || events.isEmpty()) {
            return R.fail("事件数据为空");
        }

        if (events.size() > 5000) {
            return R.fail("单次提交超过最大限制(5000条): " + events.size());
        }

        // 3. 校验每一条事件并构建行数据
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (EventBatchDTO.EventEntry event : events) {
            try {
                validateEvent(event);
                rows.add(buildRow(event, sdf));
            } catch (IllegalArgumentException e) {
                skipped.add("roleId=" + event.getRoleId() + ": " + e.getMessage());
            }
        }

        if (!skipped.isEmpty()) {
            log.warn("事件数据校验失败({}/{}): {}", skipped.size(), events.size(),
                    skipped.size() <= 5 ? skipped : skipped.subList(0, 5) + "...");
        }

        if (rows.isEmpty()) {
            return R.fail("所有事件数据均校验失败");
        }

        // 4. 提取列名
        Set<String> columnSet = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            columnSet.addAll(row.keySet());
        }
        List<String> columns = new ArrayList<>(columnSet);

        // 5. 写入 ClickHouse
        try {
            int written = clickHouseWriterService.batchWrite(
                    projectId, configJson, tableName, columns, rows);

            Map<String, Object> result = new HashMap<>();
            result.put("written", written);
            result.put("skipped", skipped.size());
            result.put("total", events.size());

            log.info("事件写入完成: table={}, appId={}, written={}, skipped={}",
                    tableName, appId, written, skipped.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("事件写入异常: table={}, appId={}", tableName, appId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("written", 0);
            result.put("skipped", skipped.size());
            result.put("error", e.getMessage());
            return R.fail(result);
        }
    }

    private void validateEvent(EventBatchDTO.EventEntry event) {
        if (StringUtils.isBlank(event.getEventTime())) {
            throw new IllegalArgumentException("event_time 不能为空");
        }
        if (event.getRoleId() == null || event.getRoleId() <= 0) {
            throw new IllegalArgumentException("role_id 无效");
        }
        if (event.getServerId() == null || event.getServerId() <= 0) {
            throw new IllegalArgumentException("server_id 无效");
        }
    }

    private Map<String, Object> buildRow(EventBatchDTO.EventEntry event, SimpleDateFormat sdf) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("event_time", event.getEventTime());
        row.put("dt", event.getEventTime().substring(0, 10));
        row.put("role_id", event.getRoleId());
        row.put("server_id", event.getServerId());
        row.put("report_time", sdf.format(new Date()));
        if (event.getFields() != null) {
            row.putAll(event.getFields());
        }
        return row;
    }
}
