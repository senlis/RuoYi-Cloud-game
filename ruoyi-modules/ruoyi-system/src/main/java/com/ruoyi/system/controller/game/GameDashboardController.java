package com.ruoyi.system.controller.game;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.datasource.ClickHouseQueryHelper;
import com.ruoyi.system.domain.GameProject;
import com.ruoyi.system.domain.GmMail;
import com.ruoyi.system.helper.GameAuthContext;
import com.ruoyi.system.mapper.GmMailMapper;
import com.ruoyi.system.service.IGameProjectService;
import com.ruoyi.system.service.IGmMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 首页仪表盘Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/dashboard")
public class GameDashboardController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(GameDashboardController.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private IGmMailService gmMailService;

    @Autowired
    private IGameProjectService gameProjectService;

    @Autowired
    private GmMailMapper gmMailMapper;

    @Autowired
    private GameAuthContext gameAuthContext;

    @Autowired(required = false)
    private ClickHouseQueryHelper clickHouseQueryHelper;

    private static final ExecutorService CH_POOL = Executors.newCachedThreadPool();

    @GetMapping("/stats")
    public AjaxResult stats() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 主库数据
        GmMail auditQuery = new GmMail();
        auditQuery.setStatus(0);
        List<GmMail> pendingList = gmMailMapper.selectList(auditQuery);
        data.put("pendingAudit", pendingList.size());
        data.put("projectCount", gameProjectService.selectGameProjectList(new GameProject()).size());

        // 最近操作
        List<GmMail> allMails = gmMailMapper.selectList(new GmMail());
        List<Map<String, Object>> recentList = new ArrayList<>();
        for (int i = 0; i < Math.min(5, allMails.size()); i++) {
            GmMail m = allMails.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", m.getTitle());
            item.put("status", m.getStatus());
            item.put("time", m.getCreatedAt());
            recentList.add(item);
        }
        data.put("recentMails", recentList);

        // ClickHouse 实时数据（管理员查全部，非管理员查有权限的项目）
        List<Long> authIds = getAuthorizedProjectIds();
        if (clickHouseQueryHelper != null) {
            List<Long> chProjectIds;
            if (authIds == null) {
                // 管理员：查所有项目
                chProjectIds = gameProjectService.selectGameProjectList(new GameProject())
                        .stream().map(GameProject::getProjectId).toList();
            } else {
                chProjectIds = authIds;
            }
            if (!chProjectIds.isEmpty()) {
                String today = LocalDate.now().format(DTF);
                data.putAll(queryClickHouseStats(chProjectIds, today));
            } else {
                data.put("todayActive", "-");
                data.put("todayNew", "-");
                data.put("todayRecharge", "-");
            }
        } else {
            data.put("todayActive", "-");
            data.put("todayNew", "-");
            data.put("todayRecharge", "-");
        }

        return success(data);
    }

    /** 获取用户有权限的项目ID列表（null=全部） */
    private List<Long> getAuthorizedProjectIds() {
        return gameAuthContext.getAuthProjectIds();
    }

    /** 并发查询所有项目的 ClickHouse 并汇总 */
    private Map<String, Object> queryClickHouseStats(List<Long> projectIds, String today) {
        List<Future<ProjectStats>> futures = new ArrayList<>();
        for (Long pid : projectIds) {
            futures.add(CH_POOL.submit(() -> querySingleProject(pid, today)));
        }

        long totalActive = 0, totalNew = 0;
        BigDecimal totalRecharge = BigDecimal.ZERO;
        int successCount = 0;

        for (Future<ProjectStats> f : futures) {
            try {
                ProjectStats ps = f.get(10, TimeUnit.SECONDS);
                if (ps != null) {
                    totalActive += ps.active;
                    totalNew += ps.newRole;
                    totalRecharge = totalRecharge.add(ps.recharge);
                    successCount++;
                }
            } catch (Exception e) {
                log.warn("ClickHouse 项目查询超时或失败", e);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        if (successCount > 0) {
            result.put("todayActive", totalActive);
            result.put("todayNew", totalNew);
            result.put("todayRecharge", "¥" + totalRecharge);
        } else {
            result.put("todayActive", "-");
            result.put("todayNew", "-");
            result.put("todayRecharge", "-");
        }
        return result;
    }

    /** 查询单个项目的 ClickHouse 统计 */
    private ProjectStats querySingleProject(Long projectId, String today) {
        try (Connection conn = clickHouseQueryHelper.getConnection(projectId);
             Statement stmt = conn.createStatement()) {

            long active = 0, newRole = 0;
            BigDecimal recharge = BigDecimal.ZERO;

            // 今日活跃（登录去重）
            String activeSql = "SELECT count(DISTINCT role_id) AS cnt"
                    + " FROM game_event_role_login"
                    + " WHERE dt = '" + today + "' AND event_type = 'login'"
                    + " AND project_id = " + projectId;
            try (ResultSet rs = stmt.executeQuery(activeSql)) {
                if (rs.next()) active = rs.getLong("cnt");
            }

            // 今日新增角色
            String newSql = "SELECT count(role_id) AS cnt FROM game_event_chara_creation"
                    + " WHERE dt = '" + today + "' AND project_id = " + projectId;
            try (ResultSet rs = stmt.executeQuery(newSql)) {
                if (rs.next()) newRole = rs.getLong("cnt");
            }

            // 今日充值
            String rechargeSql = "SELECT sum(amount) AS total FROM game_event_recharge"
                    + " WHERE dt = '" + today + "' AND order_status = 'success'"
                    + " AND project_id = " + projectId;
            try (ResultSet rs = stmt.executeQuery(rechargeSql)) {
                if (rs.next()) {
                    BigDecimal val = rs.getBigDecimal("total");
                    if (val != null) recharge = val;
                }
            }

            return new ProjectStats(active, newRole, recharge);

        } catch (Exception e) {
            log.warn("查询项目 {} ClickHouse 失败: {}", projectId, e.getMessage());
            return null;
        }
    }

    static class ProjectStats {
        final long active, newRole;
        final BigDecimal recharge;
        ProjectStats(long active, long newRole, BigDecimal recharge) {
            this.active = active; this.newRole = newRole; this.recharge = recharge;
        }
    }

    @GetMapping("/todo")
    public AjaxResult todo() {
        Map<String, Object> data = new LinkedHashMap<>();
        GmMail pending = new GmMail();
        pending.setStatus(0);
        data.put("pendingAuditCount", gmMailMapper.selectList(pending).size());
        data.put("failedCount", 0);
        return success(data);
    }
}
