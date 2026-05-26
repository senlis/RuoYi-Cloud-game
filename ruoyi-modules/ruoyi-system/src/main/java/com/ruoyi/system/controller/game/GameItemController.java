package com.ruoyi.system.controller.game;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.GameItem;
import com.ruoyi.system.service.IGameItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 游戏道具配置Controller
 * <p>
 * 按项目隔离，提供道具 Excel 导入、列表查询、删除功能。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/item")
public class GameItemController extends BaseController {

    @Autowired
    private IGameItemService gameItemService;

    /**
     * 查询道具列表
     */
    @RequiresPermissions("game:item:list")
    @GetMapping("/list")
    public TableDataInfo list(GameItem item) {
        startPage();
        List<GameItem> list = gameItemService.selectGameItemList(item);
        return getDataTable(list);
    }

    /**
     * 导入道具 Excel（全量覆盖导入）
     * <p>
     * Excel 两列：id（道具ID），name（道具名称）。
     * 导入到指定项目下，先清空该项目原有数据再批量插入。
     *
     * @param projectId 项目ID
     * @param file      Excel 文件
     */
    @RequiresPermissions("game:item:import")
    @Log(title = "道具配置", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult importItem(@RequestParam Long projectId,
                                  @RequestPart("file") MultipartFile file) throws Exception {
        if (projectId == null) {
            return AjaxResult.error("请选择项目");
        }
        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }
        ExcelUtil<GameItem> util = new ExcelUtil<>(GameItem.class);
        List<GameItem> itemList = util.importExcel(file.getInputStream());
        if (itemList == null || itemList.isEmpty()) {
            return AjaxResult.error("Excel 文件为空，请检查");
        }
        int count = gameItemService.importGameItem(projectId, itemList);
        return AjaxResult.success("导入成功，共 " + count + " 条记录");
    }

    /**
     * 删除道具
     */
    @RequiresPermissions("game:item:remove")
    @Log(title = "道具配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectId}/{itemId}")
    public AjaxResult remove(@PathVariable Long projectId, @PathVariable Long itemId) {
        return toAjax(gameItemService.deleteGameItemById(projectId, itemId));
    }

    /**
     * 清空指定项目下所有道具
     */
    @RequiresPermissions("game:item:remove")
    @Log(title = "道具配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/clear/{projectId}")
    public AjaxResult clear(@PathVariable Long projectId) {
        gameItemService.clearByProject(projectId);
        return AjaxResult.success("已清空");
    }
}
