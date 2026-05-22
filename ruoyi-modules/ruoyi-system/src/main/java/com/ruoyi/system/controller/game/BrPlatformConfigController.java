package com.ruoyi.system.controller.game;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.BrPlatformConfig;
import com.ruoyi.system.mapper.BrPlatformConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * 渠道接入平台配置Controller（管理端）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/bridge/platform")
public class BrPlatformConfigController extends BaseController {

    @Autowired
    private BrPlatformConfigMapper brPlatformConfigMapper;

    @Autowired
    private com.ruoyi.system.mapper.BrChannelArgConfigMapper brChannelArgConfigMapper;

    @RequiresPermissions("bridge:platform:list")
    @GetMapping("/list")
    public TableDataInfo list(BrPlatformConfig config) {
        startPage();
        List<BrPlatformConfig> list = brPlatformConfigMapper.selectList(config);
        return getDataTable(list);
    }

    @RequiresPermissions("bridge:platform:list")
    @GetMapping("/{platformId}")
    public AjaxResult getInfo(@PathVariable Long platformId) {
        return success(brPlatformConfigMapper.selectById(platformId));
    }

    @RequiresPermissions("bridge:platform:add")
    @Log(title = "接入平台配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BrPlatformConfig config) {
        try {
            return toAjax(brPlatformConfigMapper.insert(config));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return AjaxResult.error("该渠道已存在平台配置");
        }
    }

    @RequiresPermissions("bridge:platform:edit")
    @Log(title = "接入平台配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BrPlatformConfig config) {
        return toAjax(brPlatformConfigMapper.update(config));
    }

    @RequiresPermissions("bridge:platform:remove")
    @Log(title = "接入平台配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{platformIds}")
    public AjaxResult remove(@PathVariable Long[] platformIds) {
        String ids = java.util.Arrays.stream(platformIds).map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        return toAjax(brPlatformConfigMapper.deleteByIds(ids));
    }

    /** 渠道下拉选项 */
    @GetMapping("/channelOptions")
    public AjaxResult channelOptions() {
        return success(brChannelArgConfigMapper.selectChannelOptions());
    }

    @PostMapping("/testConnection")
    public AjaxResult testConnection(@RequestBody BrPlatformConfig config) {
        String url = "jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort()
                + "/" + config.getDbName() + "?connectTimeout=3000";
        try (Connection conn = DriverManager.getConnection(url, config.getDbUser(), config.getDbPwd())) {
            return AjaxResult.success("连接成功");
        } catch (Exception e) {
            return AjaxResult.error("连接失败: " + e.getMessage());
        }
    }
}
