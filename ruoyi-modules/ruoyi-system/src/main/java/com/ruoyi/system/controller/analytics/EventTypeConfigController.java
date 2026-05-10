package com.ruoyi.system.controller.analytics;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.GameEventTypeConfig;
import com.ruoyi.system.service.IGameEventTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 事件类型配置控制器
 * <p>
 * 用于管理 game_event_role_event 表的 event_type 参数语义配置。
 * 定义 n1~n10 / s1~s5 各字段的标签和解析规则。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/analytics/event-type")
public class EventTypeConfigController extends BaseController {

    @Autowired
    private IGameEventTypeConfigService eventTypeConfigService;

    /**
     * 查询事件类型列表
     */
    @RequiresPermissions("game:analytics:eventType")
    @GetMapping("/list")
    public TableDataInfo list(GameEventTypeConfig config) {
        startPage();
        List<GameEventTypeConfig> list = eventTypeConfigService.selectList(config);
        return getDataTable(list);
    }

    /**
     * 获取事件类型详细信息
     */
    @RequiresPermissions("game:analytics:eventType")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(eventTypeConfigService.selectById(id));
    }

    /**
     * 新增事件类型
     */
    @RequiresPermissions("game:analytics:eventType")
    @Log(title = "事件类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody GameEventTypeConfig config) {
        config.setCreateBy(SecurityUtils.getUsername());
        return toAjax(eventTypeConfigService.insert(config));
    }

    /**
     * 修改事件类型
     */
    @RequiresPermissions("game:analytics:eventType")
    @Log(title = "事件类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody GameEventTypeConfig config) {
        config.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(eventTypeConfigService.update(config));
    }

    /**
     * 删除事件类型
     */
    @RequiresPermissions("game:analytics:eventType")
    @Log(title = "事件类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(eventTypeConfigService.deleteByIds(ids));
    }
}
