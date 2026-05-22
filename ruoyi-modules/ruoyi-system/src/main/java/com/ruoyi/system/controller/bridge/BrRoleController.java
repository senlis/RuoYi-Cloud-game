package com.ruoyi.system.controller.bridge;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.BrRole;
import com.ruoyi.system.service.bridge.IBrRoleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色信息Controller（管理端）
 * <p>
 * 查询渠道分库中的 br_role 表数据，提供列表查询和Excel导出功能。
 *
 * @author ruoyi
 */
@Slf4j
@RestController
@RequestMapping("/game/bridge/role")
public class BrRoleController extends BaseController {

    @Autowired
    private IBrRoleService brRoleService;

    /**
     * 查询角色信息列表
     *
     * @param channelKey 渠道KEY（必填，用于路由到对应渠道数据库）
     * @param role       查询条件
     * @return 分页结果
     */
    @RequiresPermissions("bridge:role:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam String channelKey, BrRole role) {
        role.setChannelKey(channelKey);
        startPage();
        List<BrRole> list = brRoleService.selectRoleList(channelKey, role);
        return getDataTable(list);
    }

    /**
     * 导出角色信息列表
     *
     * @param response   HTTP响应
     * @param channelKey 渠道KEY（必填，用于路由到对应渠道数据库）
     * @param role       查询条件
     */
    @RequiresPermissions("bridge:role:export")
    @Log(title = "角色信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestParam String channelKey, BrRole role) {
        role.setChannelKey(channelKey);
        List<BrRole> list = brRoleService.selectRoleList(channelKey, role);
        ExcelUtil<BrRole> util = new ExcelUtil<>(BrRole.class);
        util.exportExcel(response, list, "角色信息");
    }
}
