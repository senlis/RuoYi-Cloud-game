package com.ruoyi.gserve;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.gserve.domain.dto.EventBatchDTO;
import com.ruoyi.gserve.service.EventReceiveService;
import com.ruoyi.gserve.strategy.EventWriteStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件接收服务单元测试
 * <p>
 * 不启动 Spring 上下文，直接 Mock 策略，测试校验逻辑和流程。
 *
 * @author ruoyi
 */
class EventReceiveServiceTest {

    private EventReceiveService service;
    private TestWriteStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TestWriteStrategy();
        service = new EventReceiveService(strategy);
    }

    @ParameterizedTest(name = "{0} - 3条")
    @ValueSource(strings = {
            "game_event_chara_creation",
            "game_event_role_login",
            "game_event_role_levelup",
            "game_event_recharge",
            "game_event_role_event",
            "game_event_item_change"
    })
    @DisplayName("6张表少量数据上传")
    void testUploadSmallBatch(String table) {
        List<Map<String, Object>> events = TestDataGenerator.buildEventEntries(table, 3);
        EventBatchDTO batch = buildBatch(table, events);

        R<Map<String, Object>> result = service.receive(batch, "test_app", 1L);

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().get("total"));
        assertEquals(3, result.getData().get("written"));
        assertEquals("test", result.getData().get("strategy"));
    }

    @ParameterizedTest(name = "{0} - 500条")
    @ValueSource(strings = {
            "game_event_chara_creation",
            "game_event_role_login",
            "game_event_role_levelup",
            "game_event_recharge",
            "game_event_role_event",
            "game_event_item_change"
    })
    @DisplayName("6张表批量数据上传(500条)")
    void testUploadLargeBatch(String table) {
        List<Map<String, Object>> events = TestDataGenerator.buildEventEntries(table, 500);
        EventBatchDTO batch = buildBatch(table, events);

        R<Map<String, Object>> result = service.receive(batch, "test_app", 1L);

        assertEquals(200, result.getCode());
        assertEquals(500, result.getData().get("total"));
        assertEquals(500, result.getData().get("written"));
    }

    @Test
    @DisplayName("空事件应返回校验失败")
    void testEmptyEvents() {
        EventBatchDTO batch = buildBatch("game_event_role_login", Collections.emptyList());
        R<Map<String, Object>> result = service.receive(batch, "test_app", 1L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMsg().contains("为空"));
    }

    @Test
    @DisplayName("非法表名应拒绝")
    void testInvalidTable() {
        List<Map<String, Object>> events = TestDataGenerator.buildEventEntries("game_event_role_login", 1);
        EventBatchDTO batch = buildBatch("nonexistent_table", events);
        R<Map<String, Object>> result = service.receive(batch, "test_app", 1L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMsg().contains("不允许写入"));
    }

    @Test
    @DisplayName("超过5000条上限应拒绝")
    void testExceedMaxBatchSize() {
        List<Map<String, Object>> events = TestDataGenerator.buildEventEntries("game_event_role_login", 5001);
        EventBatchDTO batch = buildBatch("game_event_role_login", events);
        R<Map<String, Object>> result = service.receive(batch, "test_app", 1L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMsg().contains("最大限制"));
    }

    // ========== 辅助 ==========

    private EventBatchDTO buildBatch(String table, List<Map<String, Object>> events) {
        EventBatchDTO batch = new EventBatchDTO();
        batch.setTable(table);
        batch.setAppId("test_app");

        List<EventBatchDTO.EventEntry> entries = new ArrayList<>();
        for (Map<String, Object> e : events) {
            EventBatchDTO.EventEntry entry = new EventBatchDTO.EventEntry();
            entry.setEventTime((String) e.get("eventTime"));
            entry.setRoleId((Long) e.get("roleId"));
            entry.setServerId((Long) e.get("serverId"));
            // fields = 除标准字段外的扩展属性
            Map<String, Object> fields = new LinkedHashMap<>(e);
            fields.remove("eventTime");
            fields.remove("dt");
            fields.remove("roleId");
            fields.remove("serverId");
            fields.remove("reportTime");
            entry.setFields(fields.isEmpty() ? null : fields);
            entries.add(entry);
        }
        batch.setEvents(entries);
        return batch;
    }

    /** 测试用 Mock 策略 —— 记录写入的数据并返回成功 */
    static class TestWriteStrategy implements EventWriteStrategy {
        List<String> calledTables = new ArrayList<>();

        @Override
        public int write(long projectId, String tableName, List<String> columns, List<Map<String, Object>> rows) {
            calledTables.add(tableName);
            return rows != null ? rows.size() : 0;
        }

        @Override
        public String name() { return "test"; }
    }
}
