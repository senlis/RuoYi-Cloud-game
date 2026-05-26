package com.ruoyi.system.controller.game;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.BrChannelArgConfig;
import com.ruoyi.system.domain.GameChannel;
import com.ruoyi.system.helper.GameAuthContext;
import com.ruoyi.system.mapper.BrChannelArgConfigMapper;
import com.ruoyi.system.service.IGameChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 渠道参数配置Controller
 * <p>
 * 管理界面：渠道参数CRUD + 渠道/分区下拉选项
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/bridge/channelArg")
public class BrChannelArgConfigController extends BaseController {

    @Autowired
    private BrChannelArgConfigMapper brChannelArgConfigMapper;

    @Autowired
    private IGameChannelService gameChannelService;

    @Autowired
    private GameAuthContext gameAuthContext;

    @RequiresPermissions("bridge:channelArg:list")
    @GetMapping("/list")
    public TableDataInfo list(BrChannelArgConfig config) {
        startPage();
        List<BrChannelArgConfig> list = brChannelArgConfigMapper.selectList(config);
        return getDataTable(list);
    }

    @RequiresPermissions("bridge:channelArg:list")
    @GetMapping("/{channelId}")
    public AjaxResult getInfo(@PathVariable Long channelId) {
        return success(brChannelArgConfigMapper.selectById(channelId));
    }

    @RequiresPermissions("bridge:channelArg:add")
    @Log(title = "渠道参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BrChannelArgConfig config) {
        try {
            return toAjax(brChannelArgConfigMapper.insert(config));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return AjaxResult.error("该渠道和分区已存在配置，请勿重复添加");
        }
    }

    @RequiresPermissions("bridge:channelArg:edit")
    @Log(title = "渠道参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BrChannelArgConfig config) {
        return toAjax(brChannelArgConfigMapper.update(config));
    }

    @RequiresPermissions("bridge:channelArg:remove")
    @Log(title = "渠道参数配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{channelIds}")
    public AjaxResult remove(@PathVariable Long[] channelIds) {
        String ids = Arrays.stream(channelIds).map(String::valueOf).collect(Collectors.joining(","));
        return toAjax(brChannelArgConfigMapper.deleteByIds(ids));
    }

    /** 渠道下拉选项（已按权限过滤） */
    @GetMapping("/channelOptions")
    public AjaxResult channelOptions() {
        List<GameChannel> list = gameChannelService.selectGameChannelList(new GameChannel());
        List<Map<String, Object>> options = list.stream().map(c -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("channelCode", c.getChannelCode());
            m.put("channelName", c.getChannelName());
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return success(options);
    }

    /** 分区下拉选项（已按权限过滤） */
    @GetMapping("/regionOptions/{channelCode}")
    public AjaxResult regionOptions(@PathVariable String channelCode) {
        List<Map<String, Object>> regions = brChannelArgConfigMapper.selectRegionOptions(channelCode);
        if (!gameAuthContext.isAdmin()) {
            List<Long> authIds = gameAuthContext.getAuthRegionIds();
            if (authIds != null && !authIds.isEmpty()) {
                regions = regions.stream()
                    .filter(r -> r.get("regionId") != null && authIds.contains(
                        ((Number) r.get("regionId")).longValue()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (authIds != null && authIds.isEmpty()) {
                regions = java.util.Collections.emptyList();
            }
        }
        return success(regions);
    }
}
