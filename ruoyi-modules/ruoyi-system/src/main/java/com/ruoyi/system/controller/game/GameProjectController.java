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
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.GameProject;
import com.ruoyi.system.service.IGameProjectService;

/**
 * 游戏项目 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/project")
public class GameProjectController extends BaseController
{
    @Autowired
    private IGameProjectService gameProjectService;

    /**
     * 查询游戏项目列表
     */
    @RequiresPermissions("game:project:list")
    @GetMapping("/list")
    public TableDataInfo list(GameProject project)
    {
        startPage();
        List<GameProject> list = gameProjectService.selectGameProjectList(project);
        return getDataTable(list);
    }

    /**
     * 获取游戏项目详细信息
     */
    @RequiresPermissions("game:project:list")
    @GetMapping(value = "/{projectId}")
    public AjaxResult getInfo(@PathVariable Long projectId)
    {
        return success(gameProjectService.selectGameProjectById(projectId));
    }

    /**
     * 获取所有游戏项目列表(无权限要求,用于下拉框)
     */
    @GetMapping("/all")
    public AjaxResult all()
    {
        List<GameProject> list = gameProjectService.selectGameProjectList(new GameProject());
        return success(list);
    }

    /**
     * 新增游戏项目
     */
    @RequiresPermissions("game:project:add")
    @Log(title = "游戏项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody GameProject project)
    {
        if (!gameProjectService.checkProjectCodeUnique(project))
        {
            return error("新增项目'" + project.getProjectName() + "'失败，项目编码已存在");
        }
        project.setCreateBy(SecurityUtils.getUsername());
        return toAjax(gameProjectService.insertGameProject(project));
    }

    /**
     * 修改游戏项目
     */
    @RequiresPermissions("game:project:edit")
    @Log(title = "游戏项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody GameProject project)
    {
        if (!gameProjectService.checkProjectCodeUnique(project))
        {
            return error("修改项目'" + project.getProjectName() + "'失败，项目编码已存在");
        }
        project.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(gameProjectService.updateGameProject(project));
    }

    /**
     * 删除游戏项目
     */
    @RequiresPermissions("game:project:remove")
    @Log(title = "游戏项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds)
    {
        return toAjax(gameProjectService.deleteGameProjectByIds(projectIds));
    }

    /**
     * 导出游戏项目列表
     */
    @RequiresPermissions("game:project:export")
    @Log(title = "游戏项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GameProject project)
    {
        List<GameProject> list = gameProjectService.selectGameProjectList(project);
        ExcelUtil<GameProject> util = new ExcelUtil<>(GameProject.class);
        util.exportExcel(response, list, "游戏项目数据");
    }

    /**
     * 测试 ClickHouse 连接
     */
    @RequiresPermissions("game:project:edit")
    @PostMapping("/test-clickhouse")
    public AjaxResult testClickHouse(@RequestBody Map<String, Object> config)
    {
        try
        {
            String host = (String) config.get("host");
            int port = Integer.parseInt(String.valueOf(config.get("port")));
            String database = (String) config.get("database");
            String username = (String) config.get("username");
            String password = (String) config.getOrDefault("password", "");

            String url = String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", username != null ? username : "default");
            props.setProperty("password", password);
            props.setProperty("connectTimeout", "5000");
            props.setProperty("compress", "0");

            try (java.sql.Connection conn = new com.clickhouse.jdbc.ClickHouseDataSource(url, props).getConnection())
            {
                if (conn.isValid(3))
                {
                    return success("连接成功");
                }
                else
                {
                    return error("连接失败：无法建立连接");
                }
            }
        }
        catch (Exception e)
        {
            return error("连接失败：" + e.getMessage());
        }
    }
}
