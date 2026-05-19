package com.ruoyi.system.controller.game;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.BrChannelArgConfig;
import com.ruoyi.system.mapper.BrChannelArgConfigMapper;
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

    /** 渠道下拉选项 */
    @GetMapping("/channelOptions")
    public AjaxResult channelOptions() {
        return success(brChannelArgConfigMapper.selectChannelOptions());
    }

    /** 分区下拉选项 */
    @GetMapping("/regionOptions/{channelCode}")
    public AjaxResult regionOptions(@PathVariable String channelCode) {
        return success(brChannelArgConfigMapper.selectRegionOptions(channelCode));
    }
}
