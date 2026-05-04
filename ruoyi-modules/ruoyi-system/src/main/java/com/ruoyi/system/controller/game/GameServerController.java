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
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.GameServer;
import com.ruoyi.system.service.IGameDataSourceService;
import com.ruoyi.system.service.IGameServerService;

/**
 * 游戏服务器 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/server")
public class GameServerController extends BaseController
{
    @Autowired
    private IGameServerService gameServerService;

    @Autowired
    private IGameDataSourceService gameDataSourceService;

    /**
     * 查询游戏服务器列表
     */
    @RequiresPermissions("game:server:list")
    @GetMapping("/list")
    public TableDataInfo list(GameServer server)
    {
        startPage();
        List<GameServer> list = gameServerService.selectGameServerList(server);
        return getDataTable(list);
    }

    /**
     * 获取游戏服务器详细信息
     */
    @RequiresPermissions("game:server:list")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(gameServerService.selectGameServerById(id));
    }

    /**
     * 根据分区ID查询游戏服务器列表(用于下拉框)
     */
    @GetMapping("/listByRegion/{regionId}")
    public AjaxResult listByRegion(@PathVariable Long regionId)
    {
        List<GameServer> list = gameServerService.selectGameServerByRegionId(regionId);
        return success(list);
    }

    /**
     * 新增游戏服务器
     */
    @RequiresPermissions("game:server:add")
    @Log(title = "游戏服务器", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody GameServer server)
    {
        if (!gameServerService.checkServerCodeUnique(server))
        {
            return error("新增服务器'" + server.getServerName() + "'失败，服务器ID已存在");
        }
        server.setCreateBy(SecurityUtils.getUsername());
        return toAjax(gameServerService.insertGameServer(server));
    }

    /**
     * 修改游戏服务器
     */
    @RequiresPermissions("game:server:edit")
    @Log(title = "游戏服务器", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody GameServer server)
    {
        if (!gameServerService.checkServerCodeUnique(server))
        {
            return error("修改服务器'" + server.getServerName() + "'失败，服务器ID已存在");
        }
        server.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(gameServerService.updateGameServer(server));
    }

    /**
     * 删除游戏服务器
     */
    @RequiresPermissions("game:server:remove")
    @Log(title = "游戏服务器", businessType = BusinessType.DELETE)
    @DeleteMapping("/{serverIds}")
    public AjaxResult remove(@PathVariable Long[] serverIds)
    {
        return toAjax(gameServerService.deleteGameServerByIds(serverIds));
    }

    /**
     * 刷新游戏数据源（清除缓存的数据库连接，下次访问自动重建）
     */
    @RequiresPermissions("game:server:edit")
    @Log(title = "游戏服务器", businessType = BusinessType.UPDATE)
    @PostMapping("/refreshDs/{regionId}/{serverId}")
    public AjaxResult refreshDataSource(@PathVariable Long regionId, @PathVariable Integer serverId)
    {
        gameDataSourceService.refreshAll(regionId, serverId);
        return success();
    }

    /**
     * 导出游戏服务器列表
     */
    @RequiresPermissions("game:server:export")
    @Log(title = "游戏服务器", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GameServer server)
    {
        List<GameServer> list = gameServerService.selectGameServerList(server);
        ExcelUtil<GameServer> util = new ExcelUtil<>(GameServer.class);
        util.exportExcel(response, list, "游戏服务器数据");
    }
}
