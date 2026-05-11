package com.ruoyi.gserve;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 事件上报 HTTP 集成测试
 * <p>
 * 直接发送 HTTP 请求到 gserve 服务（端口 9400），模拟游戏服真实上报流程。
 * 需要先启动 gserve 服务。
 * <p>
 * 启动方式：运行 ruoyi-gserve 的 main 方法，或 bin/run-modules-gserve.bat
 *
 * @author ruoyi
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventReceiveHttpTest {

    private static final String BASE_URL = "http://localhost:9400/etl";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @BeforeAll
    static void checkServer() throws Exception {
        // 先检查服务是否存活
        HttpRequest healthReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> healthResp = client.send(healthReq, HttpResponse.BodyHandlers.ofString());
        assumeTrue(healthResp.statusCode() == 200, "gserve 服务未启动（预期端口 9400），请先启动服务");
    }

    @Test
    @Order(1)
    @DisplayName("6张表各3条 - 正常上报")
    void testAllTablesSmallBatch() throws Exception {
        for (String table : TABLES) {
            List<Map<String, Object>> events = TestDataGenerator.buildEventEntries(table, 3);
            JSONObject body = new JSONObject();
            body.put("table", table);
            body.put("appId", "test_sdk");
            body.put("events", events);

            JSONObject resp = post("/receive", body);
            assertEquals(200, resp.getIntValue("code"), "表 " + table + " 返回失败");
            assertEquals(3, resp.getJSONObject("data").getIntValue("total"));
            System.out.println("  ✅ " + table + " → 3条 ✔");
        }
    }

    @Test
    @Order(2)
    @DisplayName("6张表各500条 - 批量上报")
    void testAllTablesLargeBatch() throws Exception {
        for (String table : TABLES) {
            List<Map<String, Object>> events = TestDataGenerator.buildEventEntries(table, 500);
            JSONObject body = new JSONObject();
            body.put("table", table);
            body.put("appId", "test_sdk");
            body.put("events", events);

            JSONObject resp = post("/receive", body);
            assertEquals(200, resp.getIntValue("code"), "表 " + table + " 批量失败");
            assertEquals(500, resp.getJSONObject("data").getIntValue("total"));
            System.out.println("  ✅ " + table + " → 500条 ✔");
        }
    }

    @Test
    @Order(3)
    @DisplayName("空事件列表应返回错误")
    void testEmptyEvents() throws Exception {
        JSONObject body = new JSONObject();
        body.put("table", "game_event_role_login");
        body.put("appId", "test_sdk");
        body.put("events", Collections.emptyList());

        JSONObject resp = post("/receive", body);
        assertEquals(500, resp.getIntValue("code"));
        assertTrue(resp.getString("msg").contains("为空"));
        System.out.println("  ✅ 空事件校验 ✔");
    }

    @Test
    @Order(4)
    @DisplayName("非法表名应拒绝")
    void testInvalidTable() throws Exception {
        List<Map<String, Object>> events = TestDataGenerator.buildEventEntries("game_event_role_login", 1);
        JSONObject body = new JSONObject();
        body.put("table", "nonexistent_table");
        body.put("appId", "test_sdk");
        body.put("events", events);

        JSONObject resp = post("/receive", body);
        assertEquals(500, resp.getIntValue("code"));
        assertTrue(resp.getString("msg").contains("不允许写入"));
        System.out.println("  ✅ 非法表名校验 ✔");
    }

    @Test
    @Order(5)
    @DisplayName("超过5000条上限应拒绝")
    void testExceedMaxBatchSize() throws Exception {
        List<Map<String, Object>> events = TestDataGenerator.buildEventEntries("game_event_role_login", 5001);
        JSONObject body = new JSONObject();
        body.put("table", "game_event_role_login");
        body.put("appId", "test_sdk");
        body.put("events", events);

        JSONObject resp = post("/receive", body);
        assertEquals(500, resp.getIntValue("code"));
        assertTrue(resp.getString("msg").contains("最大限制"));
        System.out.println("  ✅ 超限校验 ✔");
    }

    @Test
    @Order(6)
    @DisplayName("缺少 X-Project-Id 应拒绝")
    void testMissingProjectId() throws Exception {
        List<Map<String, Object>> events = TestDataGenerator.buildEventEntries("game_event_role_login", 1);
        JSONObject body = new JSONObject();
        body.put("table", "game_event_role_login");
        body.put("appId", "test_sdk");
        body.put("events", events);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/receive"))
                .header("Content-Type", "application/json")
                .header("X-App-Id", "test_sdk")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        JSONObject json = JSON.parseObject(resp.body());
        assertEquals(500, json.getIntValue("code"));
        assertTrue(json.getString("msg").contains("未指定项目 ID"));
        System.out.println("  ✅ 缺 ProjectId 校验 ✔");
    }

    // ========== 辅助 ==========

    private static final List<String> TABLES = List.of(
            "game_event_chara_creation",
            "game_event_role_login",
            "game_event_role_levelup",
            "game_event_recharge",
            "game_event_role_event",
            "game_event_item_change"
    );

    private JSONObject post(String path, JSONObject body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("X-App-Id", "test_sdk")
                .header("X-Project-Id", "1")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return JSON.parseObject(resp.body());
    }
}
