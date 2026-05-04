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
import com.ruoyi.system.domain.GameFieldDefine;
import com.ruoyi.system.service.IGameFieldDefineService;

/**
 * 动态字段定义 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/field/define")
public class GameFieldDefineController extends BaseController
{
    @Autowired
    private IGameFieldDefineService gameFieldDefineService;

    /**
     * 查询动态字段定义列表
     */
    @RequiresPermissions("game:field:list")
    @GetMapping("/list")
    public TableDataInfo list(GameFieldDefine fieldDefine)
    {
        startPage();
        List<GameFieldDefine> list = gameFieldDefineService.selectGameFieldDefineList(fieldDefine);
        return getDataTable(list);
    }

    /**
     * 获取动态字段定义详细信息
     */
    @RequiresPermissions("game:field:list")
    @GetMapping(value = "/{fieldId}")
    public AjaxResult getInfo(@PathVariable Long fieldId)
    {
        return success(gameFieldDefineService.selectGameFieldDefineById(fieldId));
    }

    /**
     * 根据实体类型查询启用的动态字段定义
     */
    @GetMapping("/byEntity/{entityType}")
    public AjaxResult byEntity(@PathVariable String entityType)
    {
        List<GameFieldDefine> list = gameFieldDefineService.selectActiveFieldByEntity(entityType);
        return success(list);
    }

    /**
     * 新增动态字段定义
     */
    @RequiresPermissions("game:field:add")
    @Log(title = "动态字段定义", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody GameFieldDefine fieldDefine)
    {
        fieldDefine.setCreateBy(SecurityUtils.getUsername());
        return toAjax(gameFieldDefineService.insertGameFieldDefine(fieldDefine));
    }

    /**
     * 修改动态字段定义
     */
    @RequiresPermissions("game:field:edit")
    @Log(title = "动态字段定义", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody GameFieldDefine fieldDefine)
    {
        fieldDefine.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(gameFieldDefineService.updateGameFieldDefine(fieldDefine));
    }

    /**
     * 删除动态字段定义
     */
    @RequiresPermissions("game:field:remove")
    @Log(title = "动态字段定义", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fieldIds}")
    public AjaxResult remove(@PathVariable Long[] fieldIds)
    {
        return toAjax(gameFieldDefineService.deleteGameFieldDefineByIds(fieldIds));
    }

    /**
     * 导出动态字段定义列表
     */
    @RequiresPermissions("game:field:export")
    @Log(title = "动态字段定义", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GameFieldDefine fieldDefine)
    {
        List<GameFieldDefine> list = gameFieldDefineService.selectGameFieldDefineList(fieldDefine);
        ExcelUtil<GameFieldDefine> util = new ExcelUtil<>(GameFieldDefine.class);
        util.exportExcel(response, list, "动态字段定义数据");
    }
}
