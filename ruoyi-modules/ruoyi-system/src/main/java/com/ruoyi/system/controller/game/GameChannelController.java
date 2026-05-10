package com.ruoyi.system.controller.game;

import java.util.List;
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
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.GameChannel;
import com.ruoyi.system.domain.GameProject;
import com.ruoyi.system.mapper.GameProjectMapper;
import com.ruoyi.system.service.IGameChannelService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 游戏渠道 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/channel")
public class GameChannelController extends BaseController
{
    @Autowired
    private IGameChannelService gameChannelService;

    @Autowired
    private GameProjectMapper gameProjectMapper;

    /**
     * 查询游戏渠道列表
     */
    @RequiresPermissions("game:channel:list")
    @GetMapping("/list")
    public TableDataInfo list(GameChannel channel)
    {
        startPage();
        List<GameChannel> list = gameChannelService.selectGameChannelList(channel);
        return getDataTable(list);
    }

    /**
     * 获取游戏渠道详细信息
     */
    @RequiresPermissions("game:channel:list")
    @GetMapping(value = "/{channelId}")
    public AjaxResult getInfo(@PathVariable Long channelId)
    {
        return success(gameChannelService.selectGameChannelById(channelId));
    }

    /**
     * 根据项目ID查询游戏渠道列表(用于下拉框)
     */
    @GetMapping("/listByProject/{projectId}")
    public AjaxResult listByProject(@PathVariable Long projectId)
    {
        List<GameChannel> list = gameChannelService.selectGameChannelByProjectId(projectId);
        return success(list);
    }

    /**
     * 新增游戏渠道
     */
    @RequiresPermissions("game:channel:add")
    @Log(title = "游戏渠道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody GameChannel channel)
    {
        if (!gameChannelService.checkChannelCodeUnique(channel))
        {
            return error("新增渠道'" + channel.getChannelName() + "'失败，渠道编码已存在");
        }
        channel.setCreateBy(SecurityUtils.getUsername());
        return toAjax(gameChannelService.insertGameChannel(channel));
    }

    /**
     * 修改游戏渠道
     */
    @RequiresPermissions("game:channel:edit")
    @Log(title = "游戏渠道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody GameChannel channel)
    {
        if (!gameChannelService.checkChannelCodeUnique(channel))
        {
            return error("修改渠道'" + channel.getChannelName() + "'失败，渠道编码已存在");
        }
        channel.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(gameChannelService.updateGameChannel(channel));
    }

    /**
     * 删除游戏渠道
     */
    @RequiresPermissions("game:channel:remove")
    @Log(title = "游戏渠道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{channelIds}")
    public AjaxResult remove(@PathVariable Long[] channelIds)
    {
        return toAjax(gameChannelService.deleteGameChannelByIds(channelIds));
    }

    /**
     * 导出游戏渠道列表
     */
    @RequiresPermissions("game:channel:export")
    @Log(title = "游戏渠道", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GameChannel channel)
    {
        List<GameChannel> list = gameChannelService.selectGameChannelList(channel);
        ExcelUtil<GameChannel> util = new ExcelUtil<>(GameChannel.class);
        util.exportExcel(response, list, "游戏渠道数据");
    }

    // ==================== 供 Auth 模块 Feign 调用的内部接口 ====================

    /**
     * 查询渠道 SecureKey 相关信息（内部 Feign 调用）
     */
    @InnerAuth
    @GetMapping("/secure-info/{channelId}")
    public AjaxResult getChannelSecureInfo(@PathVariable Long channelId) {
        GameChannel channel = gameChannelService.selectGameChannelById(channelId);
        if (channel == null) {
            return error("渠道不存在: " + channelId);
        }

        GameProject project = gameProjectMapper.selectGameProjectById(channel.getProjectId());

        Map<String, Object> info = new HashMap<>();
        info.put("channelId", channel.getChannelId());
        info.put("channelCode", channel.getChannelCode());
        info.put("projectId", channel.getProjectId());
        info.put("secureKeyHash", channel.getSecureKeyHash());
        info.put("secureKeySalt", channel.getSecureKeySalt());
        info.put("secureKeyVersion", channel.getSecureKeyVersion());
        info.put("secureKeyUpdatedAt", channel.getSecureKeyUpdatedAt());
        info.put("clickhouseConfig", project != null ? project.getClickhouseConfig() : null);

        return success(info);
    }

    /**
     * 更新渠道 SecureKey 字段（内部 Feign 调用）
     */
    @InnerAuth
    @PutMapping("/secure-key/{channelId}")
    public AjaxResult updateChannelSecureKey(@PathVariable Long channelId,
                                              @RequestBody Map<String, Object> data) {
        GameChannel channel = gameChannelService.selectGameChannelById(channelId);
        if (channel == null) {
            return error("渠道不存在: " + channelId);
        }

        if (data.containsKey("secureKeyHash")) {
            channel.setSecureKeyHash((String) data.get("secureKeyHash"));
        }
        if (data.containsKey("secureKeySalt")) {
            channel.setSecureKeySalt((String) data.get("secureKeySalt"));
        }
        if (data.containsKey("secureKeyVersion")) {
            Object ver = data.get("secureKeyVersion");
            channel.setSecureKeyVersion(ver instanceof Integer ? (Integer) ver
                    : Integer.parseInt(String.valueOf(ver)));
        }
        if (data.containsKey("secureKeyUpdatedAt")) {
            Object dt = data.get("secureKeyUpdatedAt");
            if (dt instanceof java.util.Date) {
                channel.setSecureKeyUpdatedAt((java.util.Date) dt);
            } else {
                // Feign JSON 序列化后 Date 变成 String，直接设为当前时间
                channel.setSecureKeyUpdatedAt(new java.util.Date());
            }
        }

        gameChannelService.updateGameChannel(channel);
        return success();
    }
}
