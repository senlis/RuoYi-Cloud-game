package com.ruoyi.system.controller.game;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.GameRoleAuth;
import com.ruoyi.system.service.IGameRoleAuthService;

/**
 * 游戏角色权限 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/roleAuth")
public class GameRoleAuthController extends BaseController
{
    @Autowired
    private IGameRoleAuthService gameRoleAuthService;

    /**
     * 查询游戏角色权限列表
     */
    @RequiresPermissions("game:auth:list")
    @GetMapping("/list")
    public AjaxResult list(GameRoleAuth gameRoleAuth)
    {
        List<GameRoleAuth> list = gameRoleAuthService.selectGameRoleAuthList(gameRoleAuth);
        return success(list);
    }

    /**
     * 获取游戏角色权限详细信息
     */
    @RequiresPermissions("game:auth:list")
    @GetMapping(value = "/{authId}")
    public AjaxResult getInfo(@PathVariable Long authId)
    {
        return success(gameRoleAuthService.selectGameRoleAuthById(authId));
    }

    /**
     * 查询角色在指定实体类型下的授权实体ID列表(无权限要求，供前端调用)
     */
    @GetMapping("/byRole/{roleId}/{entityType}")
    public AjaxResult getEntityIdsByRole(@PathVariable Long roleId, @PathVariable String entityType)
    {
        List<Long> entityIds = gameRoleAuthService.selectEntityIdsByRoleAndType(roleId, entityType);
        return success(entityIds);
    }

    /**
     * 保存角色权限(先删除原有权限，再批量新增)
     */
    @RequiresPermissions("game:auth:edit")
    @Log(title = "游戏角色权限", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody Map<String, Object> params)
    {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        String entityType = (String) params.get("entityType");

        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) params.get("entityIds");
        java.util.ArrayList<Long> entityIds = new java.util.ArrayList<>();
        if (rawIds != null)
        {
            for (Object id : rawIds)
            {
                entityIds.add(Long.valueOf(id.toString()));
            }
        }

        return toAjax(gameRoleAuthService.saveRoleAuth(roleId, entityType, entityIds));
    }

    /**
     * 批量删除游戏角色权限
     */
    @RequiresPermissions("game:auth:edit")
    @Log(title = "游戏角色权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/{authIds}")
    public AjaxResult remove(@PathVariable Long[] authIds)
    {
        return toAjax(gameRoleAuthService.deleteGameRoleAuthByIds(authIds));
    }
}
