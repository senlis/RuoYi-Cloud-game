package com.ruoyi.system.controller.game;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.GameRegion;
import com.ruoyi.system.service.IGameRegionService;

/**
 * 游戏分区 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/region")
public class GameRegionController extends BaseController
{
    @Autowired
    private IGameRegionService gameRegionService;

    /**
     * 查询游戏分区列表
     */
    @RequiresPermissions("game:region:list")
    @GetMapping("/list")
    public TableDataInfo list(GameRegion region)
    {
        startPage();
        List<GameRegion> list = gameRegionService.selectGameRegionList(region);
        return getDataTable(list);
    }

    /**
     * 获取游戏分区详细信息
     */
    @RequiresPermissions("game:region:list")
    @GetMapping(value = "/{regionId}")
    public AjaxResult getInfo(@PathVariable Long regionId)
    {
        return success(gameRegionService.selectGameRegionById(regionId));
    }

    /**
     * 根据渠道ID查询游戏分区列表(用于下拉框)
     */
    @GetMapping("/listByChannel/{channelId}")
    public AjaxResult listByChannel(@PathVariable Long channelId)
    {
        List<GameRegion> list = gameRegionService.selectGameRegionByChannelId(channelId);
        return success(list);
    }

    /**
     * 新增游戏分区
     */
    @RequiresPermissions("game:region:add")
    @Log(title = "游戏分区", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody GameRegion region)
    {
        if (!gameRegionService.checkRegionCodeUnique(region))
        {
            return error("新增分区'" + region.getRegionName() + "'失败，分区编码已存在");
        }
        region.setCreateBy(SecurityUtils.getUsername());
        return toAjax(gameRegionService.insertGameRegion(region));
    }

    /**
     * 修改游戏分区
     */
    @RequiresPermissions("game:region:edit")
    @Log(title = "游戏分区", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody GameRegion region)
    {
        if (!gameRegionService.checkRegionCodeUnique(region))
        {
            return error("修改分区'" + region.getRegionName() + "'失败，分区编码已存在");
        }
        region.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(gameRegionService.updateGameRegion(region));
    }

    /**
     * 删除游戏分区
     */
    @RequiresPermissions("game:region:remove")
    @Log(title = "游戏分区", businessType = BusinessType.DELETE)
    @DeleteMapping("/{regionIds}")
    public AjaxResult remove(@PathVariable Long[] regionIds)
    {
        return toAjax(gameRegionService.deleteGameRegionByIds(regionIds));
    }

    /**
     * 导出游戏分区列表
     */
    @RequiresPermissions("game:region:export")
    @Log(title = "游戏分区", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GameRegion region)
    {
        List<GameRegion> list = gameRegionService.selectGameRegionList(region);
        ExcelUtil<GameRegion> util = new ExcelUtil<>(GameRegion.class);
        util.exportExcel(response, list, "游戏分区数据");
    }

    /**
     * 生成并存储导出配置
     */
    @RequiresPermissions("game:region:exportConfig")
    @Log(title = "游戏分区", businessType = BusinessType.UPDATE)
    @PostMapping("/{regionId}/exportConfig")
    public AjaxResult exportConfig(@PathVariable Long regionId)
    {
        String config = gameRegionService.exportConfig(regionId);
        return AjaxResult.success("操作成功", config);
    }

    /**
     * 获取分区配置(无权限要求,用于游戏客户端)
     */
    @GetMapping("/{regionId}/config")
    public AjaxResult getRegionConfig(@PathVariable Long regionId)
    {
        String config = gameRegionService.getRegionConfig(regionId);
        return success(config);
    }

    /**
     * 获取分区服务器列表(无权限要求,用于游戏客户端)
     */
    @GetMapping("/{regionId}/servers")
    public AjaxResult getRegionServers(@PathVariable Long regionId)
    {
        String servers = gameRegionService.getRegionServers(regionId);
        return success(servers);
    }

    /**
     * 克隆分区
     */
    @RequiresPermissions("game:region:clone")
    @Log(title = "游戏分区", businessType = BusinessType.INSERT)
    @PostMapping("/{regionId}/clone")
    public AjaxResult clone(@PathVariable Long regionId, @RequestBody Map<String, Object> params)
    {
        String newName = (String) params.get("newName");
        String newCode = (String) params.get("newCode");
        Long targetChannelId = params.get("targetChannelId") != null
                ? Long.valueOf(params.get("targetChannelId").toString()) : null;
        boolean cloneServers = params.get("cloneServers") != null
                && Boolean.parseBoolean(params.get("cloneServers").toString());
        Long newRegionId = gameRegionService.cloneRegion(regionId, newName, newCode, targetChannelId, cloneServers);
        return success(newRegionId);
    }
}
