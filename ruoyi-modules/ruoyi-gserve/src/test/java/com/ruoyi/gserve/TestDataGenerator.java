package com.ruoyi.gserve;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 动态测试数据生成器
 * <p>
 * 按照各事件的业务规则生成仿真数据。
 * 字段名使用驼峰命名（与 EventBatchDTO 的 Jackson 序列化一致）。
 *
 * @author ruoyi
 */
public class TestDataGenerator {

    private static final ThreadLocalRandom R = ThreadLocalRandom.current();

    public static final List<Integer> SERVER_IDS = List.of(
            100, 101, 102, 103, 104,
            105, 106, 107, 108, 109,
            110, 111, 112, 113, 114,
            115, 116, 117, 118, 119
    );

    public static final List<Long> ROLE_IDS = List.of(
            10001L, 10002L, 10003L, 10004L, 10005L,
            20001L, 20002L, 20003L, 20004L, 20005L,
            30001L, 30002L, 30003L, 30004L, 30005L
    );

    public static final List<Long> ITEM_IDS = List.of(101L, 102L, 201L, 301L, 401L, 501L, 601L, 701L);
    public static final List<String> EVENT_TYPES = List.of(
            "quest_complete", "pvp_battle", "boss_kill", "friend_add",
            "guild_create", "trade_done", "achievement_unlock"
    );
    public static final int MIN_RECHARGE = 100;
    public static final int MAX_RECHARGE = 64800;

    public static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 构建 EventBatchDTO 兼容的 events JSON 列表（驼峰命名）
     * 每项: { eventTime, roleId, serverId, fields: { ... } }
     */
    public static List<Map<String, Object>> buildEventEntries(String table, int count) {
        List<Map<String, Object>> entries = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < count; i++) {
            Map<String, Object> row = generateRow(table, today);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("eventTime", row.get("eventTime"));
            entry.put("roleId", row.get("roleId"));
            entry.put("serverId", row.get("serverId"));

            // fields 里放除标准字段外的扩展属性
            Map<String, Object> fields = new LinkedHashMap<>(row);
            fields.remove("eventTime");
            fields.remove("dt");
            fields.remove("roleId");
            fields.remove("serverId");
            fields.remove("reportTime");
            entry.put("fields", fields);
            entries.add(entry);
        }
        return entries;
    }

    /** 生成单行完整数据 */
    public static Map<String, Object> generateRow(String table, LocalDate today) {
        LocalDateTime eventDt = today.atStartOfDay()
                .plusHours(R.nextInt(0, 24)).plusMinutes(R.nextInt(0, 60));
        String eventTime = eventDt.format(DT_FMT);
        String dateStr = eventDt.format(D_FMT);
        long roleId = pick(ROLE_IDS);
        int serverId = pick(SERVER_IDS);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("eventTime", eventTime);
        row.put("dt", dateStr);
        row.put("roleId", roleId);
        row.put("serverId", (long) serverId);
        row.put("reportTime", LocalDateTime.now().format(DT_FMT));

        // fields 中的键名使用 ClickHouse 列名（snake_case，对应 clickhouse_game_analytics.sql）
        switch (table) {
            case "game_event_chara_creation":
                row.put("role_name", randomName());
                row.put("level", R.nextInt(1, 120));
                row.put("vocation", R.nextInt(1, 6));
                row.put("gender", R.nextInt(0, 2));
                row.put("ip", randomIp());
                row.put("device_os", pick(List.of("iOS", "Android", "HarmonyOS")));
                row.put("client_version", R.nextInt(1, 5) + "." + R.nextInt(0, 10) + "." + R.nextInt(0, 100));
                break;
            case "game_event_role_login":
                row.put("event_type", pick(List.of("login", "logout")));
                row.put("online_duration", R.nextInt(60, 36000));
                row.put("level", R.nextInt(1, 120));
                row.put("ip", randomIp());
                row.put("device_os", pick(List.of("iOS", "Android", "HarmonyOS")));
                row.put("client_version", R.nextInt(1, 5) + "." + R.nextInt(0, 10) + "." + R.nextInt(0, 100));
                break;
            case "game_event_role_levelup":
                row.put("old_level", R.nextInt(1, 80));
                row.put("new_level", R.nextInt(80, 121));
                row.put("cost_seconds", R.nextLong(3600L, 86400L * 7));
                row.put("vocation", R.nextInt(1, 6));
                break;
            case "game_event_recharge":
                row.put("amount", R.nextDouble(1, 648));
                row.put("order_id", "ORD" + System.nanoTime() + R.nextInt(1000, 9999));
                row.put("product_id", "item_gold_" + R.nextInt(1, 10));
                row.put("product_name", pick(List.of("60钻石", "300钻石", "648钻石", "月卡", "周卡")));
                row.put("pay_type", pick(List.of("alipay", "wechat", "apple", "google")));
                row.put("currency", "CNY");
                row.put("order_status", "success");
                row.put("is_first_recharge", R.nextInt(10) == 0 ? 1 : 0);
                break;
            case "game_event_role_event":
                row.put("event_type", pick(EVENT_TYPES));
                row.put("event_id", R.nextInt(1001, 9999));
                row.put("event_value", R.nextDouble(0, 10000));
                row.put("n1", R.nextInt(0, 100));
                row.put("n2", R.nextInt(0, 100));
                row.put("duration_ms", R.nextInt(100, 600000));
                row.put("ip", randomIp());
                break;
            case "game_event_item_change":
                long itemId = pick(ITEM_IDS);
                row.put("item_id", itemId);
                row.put("item_name", "item_" + itemId);
                row.put("opt_id", R.nextInt(1, 20));
                row.put("opt_type", R.nextInt(1, 5));
                row.put("item_num", R.nextInt(1, 100));
                row.put("before_num", R.nextInt(0, 10000));
                row.put("after_num", R.nextInt(0, 10000));
                break;
        }
        return row;
    }

    private static <T> T pick(List<T> list) { return list.get(R.nextInt(list.size())); }

    private static String randomName() {
        String[] first = {"剑", "龙", "风", "云", "月", "星", "天", "雪", "影", "焰"};
        String[] last = {"魂", "舞", "歌", "岚", "霜", "尘", "翎", "刃", "翼", "芒"};
        return pick(Arrays.asList(first)) + pick(Arrays.asList(last)) + R.nextInt(1, 999);
    }

    private static String randomIp() {
        return R.nextInt(10, 223) + "." + R.nextInt(0, 256) + "." + R.nextInt(0, 256) + "." + R.nextInt(1, 255);
    }
}
