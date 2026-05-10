package com.ruoyi.system.controller.analytics;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.service.game.analytics.AnalyticsQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据统计分析查询控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsQueryController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsQueryController.class);

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsQueryController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/new-role/daily")
    public TableDataInfo getNewRoleDaily(@RequestParam Long projectId,
                                         @RequestParam(required = false) String serverIds,
                                         @RequestParam String beginDate,
                                         @RequestParam String endDate) {
        startPage();
        List<Map<String, Object>> list = analyticsQueryService.getNewRoleDaily(projectId, serverIds, beginDate, endDate);
        return getDataTable(list);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/dau/daily")
    public TableDataInfo getDauDaily(@RequestParam Long projectId,
                                     @RequestParam(required = false) String serverIds,
                                     @RequestParam String beginDate,
                                     @RequestParam String endDate) {
        startPage();
        List<Map<String, Object>> list = analyticsQueryService.getDauDaily(projectId, serverIds, beginDate, endDate);
        return getDataTable(list);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/retention")
    public AjaxResult getRetention(@RequestParam Long projectId,
                                   @RequestParam(required = false) String serverIds,
                                   @RequestParam String createDate,
                                   @RequestParam(defaultValue = "1,3,7,14,30") String gapDays) {
        List<Map<String, Object>> result = analyticsQueryService.getRetention(projectId, serverIds, createDate, gapDays);
        return success(result);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/revenue/daily")
    public TableDataInfo getRevenueDaily(@RequestParam Long projectId,
                                         @RequestParam(required = false) String serverIds,
                                         @RequestParam String beginDate,
                                         @RequestParam String endDate) {
        startPage();
        List<Map<String, Object>> list = analyticsQueryService.getRevenueDaily(projectId, serverIds, beginDate, endDate);
        return getDataTable(list);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/ltv")
    public AjaxResult getLtv(@RequestParam Long projectId,
                             @RequestParam(required = false) String serverIds,
                             @RequestParam String createDate,
                             @RequestParam(defaultValue = "1,3,7,14,30") String gapDays) {
        List<Map<String, Object>> result = analyticsQueryService.getLtv(projectId, serverIds, createDate, gapDays);
        return success(result);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/first-recharge-rate")
    public AjaxResult getFirstRechargeRate(@RequestParam Long projectId,
                                           @RequestParam(required = false) String serverIds,
                                           @RequestParam String beginDate,
                                           @RequestParam String endDate) {
        List<Map<String, Object>> result = analyticsQueryService.getFirstRechargeRate(projectId, serverIds, beginDate, endDate);
        return success(result);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/level-distribution")
    public AjaxResult getLevelDistribution(@RequestParam Long projectId,
                                           @RequestParam(required = false) String serverIds,
                                           @RequestParam String date) {
        List<Map<String, Object>> result = analyticsQueryService.getLevelDistribution(projectId, serverIds, date);
        return success(result);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/levelup-speed")
    public AjaxResult getLevelupSpeed(@RequestParam Long projectId,
                                      @RequestParam(required = false) String serverIds,
                                      @RequestParam Integer fromLevel,
                                      @RequestParam Integer toLevel) {
        List<Map<String, Object>> result = analyticsQueryService.getLevelupSpeed(projectId, serverIds, fromLevel, toLevel);
        return success(result);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/event-summary")
    public TableDataInfo getEventSummary(@RequestParam Long projectId,
                                         @RequestParam(required = false) String serverIds,
                                         @RequestParam String eventType,
                                         @RequestParam String beginDate,
                                         @RequestParam String endDate) {
        startPage();
        List<Map<String, Object>> list = analyticsQueryService.getEventSummary(projectId, serverIds, eventType, beginDate, endDate);
        return getDataTable(list);
    }

    @RequiresPermissions("game:analytics:eventLog")
    @GetMapping("/event-log")
    public TableDataInfo getEventLog(@RequestParam Long projectId,
                                     @RequestParam(required = false) String serverIds,
                                     @RequestParam(required = false) String roleSearch,
                                     @RequestParam(required = false) String eventTypes,
                                     @RequestParam(required = false) String beginDate,
                                     @RequestParam(required = false) String endDate) {
        startPage();
        List<Map<String, Object>> list = analyticsQueryService.getEventLog(projectId, serverIds, roleSearch, eventTypes, beginDate, endDate);
        return getDataTable(list);
    }

    @RequiresPermissions("game:analytics:list")
    @GetMapping("/item-summary")
    public AjaxResult getItemSummary(@RequestParam Long projectId,
                                     @RequestParam(required = false) String serverIds,
                                     @RequestParam Long itemId,
                                     @RequestParam String beginDate,
                                     @RequestParam String endDate) {
        List<Map<String, Object>> result = analyticsQueryService.getItemSummary(projectId, serverIds, itemId, beginDate, endDate);
        return success(result);
    }

    /** 批量留存（日期范围） */
    @RequiresPermissions("game:analytics:list")
    @GetMapping("/retention-batch")
    public AjaxResult getRetentionBatch(@RequestParam Long projectId,
                                        @RequestParam(required = false) String serverIds,
                                        @RequestParam String beginDate,
                                        @RequestParam String endDate,
                                        @RequestParam(defaultValue = "1,2,3,4,5,6,7,14,30") String gapDays) {
        log.debug("[analytics] retention-batch serverIds={} begin={} end={}", serverIds, beginDate, endDate);
        List<Map<String, Object>> result = analyticsQueryService.getRetentionBatch(projectId, serverIds, beginDate, endDate, gapDays);
        return success(result);
    }

    /** 批量 LTV（日期范围） */
    @RequiresPermissions("game:analytics:list")
    @GetMapping("/ltv-batch")
    public AjaxResult getLtvBatch(@RequestParam Long projectId,
                                  @RequestParam(required = false) String serverIds,
                                  @RequestParam String beginDate,
                                  @RequestParam String endDate,
                                  @RequestParam(defaultValue = "1,2,3,4,5,6,7,14,30") String gapDays) {
        log.debug("[analytics] ltv-batch serverIds={} begin={} end={}", serverIds, beginDate, endDate);
        List<Map<String, Object>> result = analyticsQueryService.getLtvBatch(projectId, serverIds, beginDate, endDate, gapDays);
        return success(result);
    }

    /** 每日总览 */
    @RequiresPermissions("game:analytics:list")
    @GetMapping("/overview")
    public AjaxResult getOverview(@RequestParam Long projectId,
                                  @RequestParam(required = false) String serverIds,
                                  @RequestParam String beginDate,
                                  @RequestParam String endDate) {
        List<Map<String, Object>> result = analyticsQueryService.getOverview(projectId, serverIds, beginDate, endDate);
        return success(result);
    }
}
