package com.ruoyi.system.service.game.analytics;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.datasource.ClickHouseQueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * 数据统计分析查询服务 — 支持多服查询
 */
@Service
public class AnalyticsQueryService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsQueryService.class);
    private final ClickHouseQueryHelper chHelper;

    public AnalyticsQueryService(ClickHouseQueryHelper chHelper) {
        this.chHelper = chHelper;
    }

    /** 构建 server IN 子句，支持表别名限定 */
    private String serverFilter(String serverIds) { return serverFilter(serverIds, null); }
    private String serverFilter(String serverIds, String alias) {
        if (StringUtils.isBlank(serverIds)) return "";
        String col = (alias != null ? alias + "." : "") + "server_id";
        return "AND " + col + " IN (" + serverIds + ")";
    }

    /** 每日总览 — Java 端合并多个独立查询 */
    public List<Map<String, Object>> getOverview(Long projectId, String serverIds,
                                                  String beginDate, String endDate) {
        String sf = serverFilter(serverIds);
        String lsf = serverFilter(serverIds, "l");

        List<Map<String, Object>> reg = query(projectId,
            "SELECT dt, server_id, count(DISTINCT role_id) AS reg_roles FROM game_event_chara_creation WHERE project_id="
            + projectId + " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " + sf + " GROUP BY dt, server_id");
        List<Map<String, Object>> login = query(projectId,
            "SELECT dt, server_id, count(DISTINCT role_id) AS login_roles FROM game_event_role_login WHERE event_type='login' AND project_id="
            + projectId + " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " + sf + " GROUP BY dt, server_id");
        List<Map<String, Object>> pay = query(projectId,
            "SELECT dt, server_id, count(DISTINCT role_id) AS pay_roles, count() AS pay_times, sum(amount) AS pay_amount FROM game_event_recharge WHERE order_status='success' AND project_id="
            + projectId + " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " + sf + " GROUP BY dt, server_id");
        List<Map<String, Object>> nu = query(projectId,
            "SELECT l.dt, l.server_id, count(DISTINCT l.role_id) AS new_login, count(DISTINCT p.role_id) AS new_pay_roles, coalesce(sum(p.amount),0) AS new_pay_amount FROM game_event_role_login l INNER JOIN game_event_chara_creation c ON l.role_id=c.role_id AND c.server_id=l.server_id AND c.dt=l.dt LEFT JOIN game_event_recharge p ON l.role_id=p.role_id AND l.server_id=p.server_id AND l.dt=p.dt AND p.order_status='success' WHERE l.event_type='login' AND l.project_id="
            + projectId + " AND l.dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " + lsf + " GROUP BY l.dt, l.server_id");

        // 合并：以 (dt, server_id) 为 key
        Map<String, Map<String, Object>> merged = new java.util.LinkedHashMap<>();
        mergeList(merged, reg, "reg_roles");
        mergeList(merged, login, "login_roles");
        mergeList(merged, pay, "pay_roles", "pay_times", "pay_amount");
        mergeList(merged, nu, "new_login", "new_pay_roles", "new_pay_amount");

        List<Map<String, Object>> result = new java.util.ArrayList<>(merged.values());
        result.sort((a, b) -> {
            int d = String.valueOf(a.get("dt")).compareTo(String.valueOf(b.get("dt")));
            if (d != 0) return d;
            return Long.compare(longVal(a, "server_id"), longVal(b, "server_id"));
        });
        // 计算派生指标 + 确保所有字段有默认值
        for (Map<String, Object> row : result) {
            long regR = longVal(row, "reg_roles");
            long loginR = longVal(row, "login_roles");
            long payR = longVal(row, "pay_roles");
            double payAmt = doubleVal(row, "pay_amount");
            long payT = longVal(row, "pay_times");
            long newL = longVal(row, "new_login");
            long newP = longVal(row, "new_pay_roles");
            double newAmt = doubleVal(row, "new_pay_amount");

            row.put("avg_pay_times", payR > 0 ? round2(payT * 1.0 / payR) : 0);
            row.put("pay_rate", loginR > 0 ? round2(payR * 100.0 / loginR) : 0);
            row.put("pay_arpu", loginR > 0 ? round2(payAmt / loginR) : 0);
            row.put("pay_arppu", payR > 0 ? round2(payAmt / payR) : 0);
            row.put("new_pay_rate", newL > 0 ? round2(newP * 100.0 / newL) : 0);
            row.put("new_login_arpu", newL > 0 ? round2(newAmt / newL) : 0);
            row.put("new_pay_arpu", newP > 0 ? round2(newAmt / newP) : 0);
            long oldL = loginR - newL;
            long oldP = payR - newP;
            double oldAmt = payAmt - newAmt;
            row.put("old_login", Math.max(0, oldL));
            row.put("old_pay_roles", Math.max(0, oldP));
            row.put("old_pay_amount", Math.max(0, oldAmt));
            row.put("old_pay_rate", oldL > 0 ? round2(oldP * 100.0 / oldL) : 0);
            row.put("old_login_arpu", oldL > 0 ? round2(oldAmt / oldL) : 0);
            row.put("old_pay_arpu", oldP > 0 ? round2(oldAmt / oldP) : 0);
            // 留存/LTV 暂空
            row.put("ret_d1", 0); row.put("ret_d2", 0); row.put("ret_d3", 0);
            row.put("ret_d4", 0); row.put("ret_d5", 0); row.put("ret_d6", 0);
            row.put("ret_d7", 0); row.put("ret_d14", 0); row.put("ret_d30", 0);
            row.put("ltv1", 0); row.put("ltv2", 0); row.put("ltv3", 0);
            row.put("ltv4", 0); row.put("ltv5", 0); row.put("ltv6", 0);
            row.put("ltv7", 0); row.put("ltv14", 0); row.put("ltv30", 0);
        }
        return result;
    }

    private void mergeList(Map<String, Map<String, Object>> merged, List<Map<String, Object>> list, String... cols) {
        for (Map<String, Object> row : list) {
            Object dtObj = row.get("dt"), sidObj = row.get("server_id");
            if (dtObj == null || sidObj == null) continue;
            String key = dtObj + "|" + sidObj;
            Map<String, Object> m = merged.computeIfAbsent(key, k -> {
                Map<String, Object> r = new java.util.LinkedHashMap<>();
                r.put("dt", dtObj);
                r.put("server_id", sidObj);
                return r;
            });
            for (String col : cols) {
                Object val = row.get(col);
                if (val != null) m.put(col, val); // 有值就覆盖，不用 putIfAbsent
            }
        }
    }

    private long longVal(Map<String, Object> row, String col) {
        Object v = row.get(col);
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0; }
    }
    private double doubleVal(Map<String, Object> row, String col) {
        Object v = row.get(col);
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; }
    }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    public List<Map<String, Object>> getNewRoleDaily(Long projectId, String serverIds,
                                                     String beginDate, String endDate) {
        String sql = "SELECT dt, server_id, count(DISTINCT role_id) AS new_roles " +
                "FROM game_event_chara_creation WHERE project_id = " + projectId +
                " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
                serverFilter(serverIds) + " GROUP BY dt, server_id ORDER BY dt, server_id";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getDauDaily(Long projectId, String serverIds,
                                                  String beginDate, String endDate) {
        String sql = "SELECT dt, server_id, count(DISTINCT role_id) AS dau " +
                "FROM game_event_role_login WHERE event_type = 'login' AND project_id = " + projectId +
                " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
                serverFilter(serverIds) + " GROUP BY dt, server_id ORDER BY dt, server_id";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getRetention(Long projectId, String serverIds,
                                                   String createDate, String gapDays) {
        String[] days = gapDays.split(",");
        List<String> selects = new ArrayList<>();
        for (String day : days) {
            int gap = Integer.parseInt(day.trim());
            selects.add("countIf(log.dt = c.create_dt + INTERVAL " + gap + " DAY) AS day_" + gap);
        }
        String sql = "SELECT c.create_dt, count(DISTINCT c.role_id) AS total, " +
                String.join(", ", selects) + " " +
                "FROM (SELECT role_id, dt AS create_dt FROM game_event_chara_creation " +
                "WHERE project_id = " + projectId + " AND dt = '" + createDate + "' " +
                serverFilter(serverIds) + ") c " +
                "LEFT JOIN game_event_role_login log ON c.role_id = log.role_id AND log.event_type = 'login' " +
                "GROUP BY c.create_dt";
        return query(projectId, sql);
    }

    /** 批量留存 — Java合并 */
    public List<Map<String, Object>> getRetentionBatch(Long projectId, String serverIds,
                                                        String beginDate, String endDate, String gapDays) {
        String[] days = gapDays.split(",");
        // 1. 查询创角基座
        List<Map<String, Object>> base = query(projectId,
            "SELECT dt AS create_dt, server_id, count(DISTINCT role_id) AS total FROM game_event_chara_creation c " +
            "WHERE c.project_id = " + projectId + " AND c.dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
            serverFilter(serverIds, "c") + " GROUP BY c.dt, c.server_id");

        // 2. 查询每日登录（去重 role_id per server）
        List<Map<String, Object>> logins = query(projectId,
            "SELECT role_id, server_id, dt FROM game_event_role_login " +
            "WHERE project_id = " + projectId + " AND event_type = 'login' AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
            serverFilter(serverIds));

        // 3. Java 端计算留存（用 Set 对同一角色去重）
        Map<String, Map<String, Object>> merged = new java.util.LinkedHashMap<>();
        Map<String, java.util.Set<String>> retained = new HashMap<>(); // key="cdt|server_id|dayN"
        for (Map<String, Object> r : base) {
            String bkey = r.get("create_dt") + "|" + r.get("server_id");
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("create_dt", r.get("create_dt"));
            m.put("server_id", r.get("server_id"));
            m.put("total", r.get("total"));
            merged.put(bkey, m);
        }
        for (Map<String, Object> lr : logins) {
            String ldt = String.valueOf(lr.get("dt"));
            String sid = String.valueOf(lr.get("server_id"));
            String rid = String.valueOf(lr.get("role_id"));
            for (Map.Entry<String, Map<String, Object>> e : merged.entrySet()) {
                String cdt = String.valueOf(e.getValue().get("create_dt"));
                if (!sid.equals(String.valueOf(e.getValue().get("server_id")))) continue;
                for (String day : days) {
                    int gap = Integer.parseInt(day.trim());
                    if (ldt.equals(shiftDate(cdt, gap))) {
                        String rk = e.getKey() + "|day" + gap;
                        retained.computeIfAbsent(rk, k -> new java.util.HashSet<>()).add(rid);
                    }
                }
            }
        }
        // 转比率
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> e : merged.entrySet()) {
            Map<String, Object> m = e.getValue();
            long total = ((Number)m.get("total")).longValue();
            for (String day : days) {
                int gap = Integer.parseInt(day.trim());
                String rk = e.getKey() + "|day" + gap;
                java.util.Set<String> set = retained.get(rk);
                long cnt = set != null ? set.size() : 0;
                m.put("day_" + gap, total > 0 ? round2(cnt * 1.0 / total) : 0);
            }
            result.add(m);
        }
        result.sort((a,b) -> String.valueOf(a.get("create_dt")).compareTo(String.valueOf(b.get("create_dt"))));
        return result;
    }

    private String shiftDate(String dt, int days) {
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(dt);
            return d.plusDays(days).toString();
        } catch (Exception e) { return dt; }
    }

    /** 批量 LTV（按日期范围逐天查，合并结果） */
    public List<Map<String, Object>> getLtvBatch(Long projectId, String serverIds,
                                                  String beginDate, String endDate, String gapDays) {
        String[] days = gapDays.split(",");
        String sql = "SELECT c.dt AS create_dt, c.server_id, count(DISTINCT c.role_id) AS total, ";
        for (String day : days) {
            int gap = Integer.parseInt(day.trim());
            sql += "round(coalesce(sumIf(r.amount, r.event_time <= c.dt + INTERVAL " + gap + " DAY), 0) / count(DISTINCT c.role_id), 2) AS ltv_" + gap + ", ";
        }
        sql = sql.substring(0, sql.length() - 2) +
              " FROM game_event_chara_creation c " +
              "LEFT JOIN game_event_recharge r ON c.role_id = r.role_id AND c.server_id = r.server_id AND r.order_status = 'success' " +
              "WHERE c.project_id = " + projectId + " AND c.dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
              serverFilter(serverIds, "c") + " GROUP BY c.dt, c.server_id ORDER BY c.dt, c.server_id";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getRevenueDaily(Long projectId, String serverIds,
                                                      String beginDate, String endDate) {
        String sql = "SELECT dt, server_id, sum(amount) AS revenue, " +
                "count(DISTINCT role_id) AS paying_users, round(avg(amount), 2) AS arppu " +
                "FROM game_event_recharge WHERE order_status = 'success' AND project_id = " + projectId +
                " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
                serverFilter(serverIds) + " GROUP BY dt, server_id ORDER BY dt, server_id";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getLtv(Long projectId, String serverIds,
                                             String createDate, String gapDays) {
        String[] days = gapDays.split(",");
        List<String> selects = new ArrayList<>();
        for (String day : days) {
            int gap = Integer.parseInt(day.trim());
            selects.add("round(sumIf(r.amount, toDate(r.event_time) <= c.create_dt + INTERVAL " +
                    gap + " DAY) / count(DISTINCT c.role_id), 2) AS ltv_" + gap);
        }
        String sql = "SELECT c.create_dt, count(DISTINCT c.role_id) AS cohort_size, " +
                String.join(", ", selects) + " " +
                "FROM (SELECT role_id, dt AS create_dt FROM game_event_chara_creation " +
                "WHERE project_id = " + projectId + " AND dt = '" + createDate + "' " +
                serverFilter(serverIds) + ") c " +
                "LEFT JOIN game_event_recharge r ON c.role_id = r.role_id AND r.order_status = 'success' " +
                "GROUP BY c.create_dt";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getFirstRechargeRate(Long projectId, String serverIds,
                                                           String beginDate, String endDate) {
        String sql = "SELECT dt, server_id, count(DISTINCT role_id) AS first_payers " +
                "FROM game_event_recharge WHERE is_first_recharge = 1 AND order_status = 'success' " +
                "AND project_id = " + projectId + " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
                serverFilter(serverIds) + " GROUP BY dt, server_id ORDER BY dt, server_id";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getLevelDistribution(Long projectId, String serverIds, String date) {
        String sql = "SELECT new_level, count(DISTINCT role_id) AS role_count " +
                "FROM game_event_role_levelup WHERE project_id = " + projectId +
                " AND dt = '" + date + "' " + serverFilter(serverIds) +
                " GROUP BY new_level ORDER BY new_level";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getLevelupSpeed(Long projectId, String serverIds,
                                                      Integer fromLevel, Integer toLevel) {
        String sql = "SELECT server_id, round(avg(cost_seconds) / 3600, 2) AS avg_hours, " +
                "count(DISTINCT role_id) AS role_count FROM game_event_role_levelup " +
                "WHERE project_id = " + projectId + " AND old_level = " + fromLevel +
                " AND new_level = " + toLevel + " " + serverFilter(serverIds) + " GROUP BY server_id";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getEventSummary(Long projectId, String serverIds,
                                                      String eventType, String beginDate, String endDate) {
        String sql = "SELECT dt, server_id, n1, n2, count(DISTINCT role_id) AS players, " +
                "count() AS total_events FROM game_event_role_event " +
                "WHERE event_type = '" + eventType + "' AND project_id = " + projectId +
                " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
                serverFilter(serverIds) + " GROUP BY dt, server_id, n1, n2 ORDER BY dt, server_id";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getEventLog(Long projectId, String serverIds,
                                                  String roleSearch, String eventTypes,
                                                  String beginDate, String endDate) {
        // 未传日期时默认查当天
        if (StringUtils.isBlank(beginDate)) beginDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        if (StringUtils.isBlank(endDate)) endDate = beginDate;
        StringBuilder where = new StringBuilder("project_id = " + projectId +
                " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "'");
        if (StringUtils.isNotBlank(serverIds)) where.append(" AND server_id IN (").append(serverIds).append(")");
        if (StringUtils.isNotBlank(roleSearch)) {
            // 尝试按数字 role_id 查，否则按角色名模糊查
            try { Long.parseLong(roleSearch); where.append(" AND role_id = ").append(roleSearch); }
            catch (NumberFormatException e) { where.append(" AND s2 LIKE '%").append(roleSearch).append("%'"); }
        }
        if (StringUtils.isNotBlank(eventTypes)) {
            where.append(" AND event_type IN ('").append(eventTypes.replace(",", "','")).append("')");
        }
        String sql = "SELECT event_time, server_id, role_id, role_name, level, vip, fight, event_type, " +
                "n1, n2, n3, n4, n5, s1, s2 FROM game_event_role_event " +
                "WHERE " + where + " ORDER BY event_time DESC";
        return query(projectId, sql);
    }

    public List<Map<String, Object>> getItemSummary(Long projectId, String serverIds,
                                                     Long itemId, String beginDate, String endDate) {
        String sql = "SELECT dt, server_id, sumIf(item_num, item_num > 0) AS total_output, " +
                "sumIf(abs(item_num), item_num < 0) AS total_consume, count(DISTINCT role_id) AS players " +
                "FROM game_event_item_change WHERE item_id = " + itemId + " AND project_id = " + projectId +
                " AND dt BETWEEN '" + beginDate + "' AND '" + endDate + "' " +
                serverFilter(serverIds) + " GROUP BY dt, server_id ORDER BY dt, server_id";
        return query(projectId, sql);
    }

    private List<Map<String, Object>> query(Long projectId, String sql) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = chHelper.getConnection(projectId);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                }
                result.add(row);
            }
            log.debug("[analytics] SQL rows={} projectId={} sql={}", result.size(), projectId, sql);
        } catch (Exception e) {
            log.error("ClickHouse 查询异常: projectId={}", projectId, e);
            throw new RuntimeException("ClickHouse 查询失败: " + e.getMessage(), e);
        }
        return result;
    }
}
