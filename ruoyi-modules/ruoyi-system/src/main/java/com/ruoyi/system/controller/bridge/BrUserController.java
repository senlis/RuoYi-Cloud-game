package com.ruoyi.system.controller.bridge;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.BrUser;
import com.ruoyi.system.service.bridge.IBrUserService;
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
 * 渠道用户Controller（管理端）
 * <p>
 * 查询渠道分库中的 br_user 表数据，提供列表查询和Excel导出功能。
 *
 * @author ruoyi
 */
@Slf4j
@RestController
@RequestMapping("/game/bridge/user")
public class BrUserController extends BaseController {

    @Autowired
    private IBrUserService brUserService;

    /**
     * 查询渠道用户列表
     *
     * @param channelKey 渠道KEY（必填，用于路由到对应渠道数据库）
     * @param user       查询条件
     * @return 分页结果
     */
    @RequiresPermissions("bridge:user:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam String channelKey, BrUser user) {
        user.setChannelKey(channelKey);
        startPage();
        List<BrUser> list = brUserService.selectUserList(channelKey, user);
        return getDataTable(list);
    }

    /**
     * 导出渠道用户列表
     *
     * @param response   HTTP响应
     * @param channelKey 渠道KEY（必填，用于路由到对应渠道数据库）
     * @param user       查询条件
     */
    @RequiresPermissions("bridge:user:export")
    @Log(title = "渠道用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestParam String channelKey, BrUser user) {
        user.setChannelKey(channelKey);
        List<BrUser> list = brUserService.selectUserList(channelKey, user);
        ExcelUtil<BrUser> util = new ExcelUtil<>(BrUser.class);
        util.exportExcel(response, list, "渠道用户");
    }
}
