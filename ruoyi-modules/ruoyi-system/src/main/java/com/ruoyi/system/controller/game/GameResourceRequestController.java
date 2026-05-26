package com.ruoyi.system.controller.game;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.GameResourceRequest;
import com.ruoyi.system.mapper.GameResourceRequestMapper;
import com.ruoyi.system.service.IGameResourceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内部资源申请Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/resource")
public class GameResourceRequestController extends BaseController {

    @Autowired
    private IGameResourceRequestService resourceRequestService;

    @Autowired
    private GameResourceRequestMapper resourceRequestMapper;

    @RequiresPermissions("gm:resource:list")
    @GetMapping("/list")
    public TableDataInfo list(GameResourceRequest req) {
        startPage();
        List<GameResourceRequest> list = resourceRequestService.selectList(req);
        return getDataTable(list);
    }

    /** 查询有权限审批当前项目的人员 */
    @GetMapping("/approvers")
    public AjaxResult approvers(@RequestParam Long projectId) {
        return success(resourceRequestMapper.selectApprovers(projectId));
    }

    @RequiresPermissions("gm:resource:list")
    @GetMapping("/{requestId}")
    public AjaxResult getInfo(@PathVariable Long requestId) {
        return success(resourceRequestService.selectById(requestId));
    }

    @RequiresPermissions("gm:resource:add")
    @Log(title = "资源申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GameResourceRequest req) {
        req.setApplicant(SecurityUtils.getUsername());
        return toAjax(resourceRequestService.insert(req));
    }

    @RequiresPermissions("gm:resource:edit")
    @Log(title = "资源申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GameResourceRequest req) {
        return toAjax(resourceRequestService.update(req));
    }

    @RequiresPermissions("gm:resource:remove")
    @Log(title = "资源申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{requestId}")
    public AjaxResult remove(@PathVariable Long requestId) {
        return toAjax(resourceRequestService.deleteById(requestId));
    }

    @RequiresPermissions("gm:resource:edit")
    @Log(title = "资源申请", businessType = BusinessType.UPDATE)
    @PutMapping("/submit/{requestId}")
    public AjaxResult submit(@PathVariable Long requestId) {
        return toAjax(resourceRequestService.submitForAudit(requestId));
    }

    // ===== 审批接口 =====

    @RequiresPermissions("gm:resource:audit")
    @GetMapping("/audit/list")
    public TableDataInfo auditList(GameResourceRequest req) {
        if (req.getStatus() == null) req.setStatus(0);
        startPage();
        List<GameResourceRequest> list = resourceRequestService.selectList(req);
        return getDataTable(list);
    }

    @RequiresPermissions("gm:resource:audit")
    @Log(title = "资源审批", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/approve/{requestId}")
    public AjaxResult approve(@PathVariable Long requestId) {
        resourceRequestService.approve(requestId);
        GameResourceRequest r = resourceRequestService.selectById(requestId);
        AjaxResult res = success("审批通过");
        if (r != null) {
            res.put("status", r.getStatus());
            res.put("failedServerIds", r.getFailedServerIds());
        }
        return res;
    }

    @RequiresPermissions("gm:resource:audit")
    @Log(title = "资源审批", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/reject/{requestId}")
    public AjaxResult reject(@PathVariable Long requestId, @RequestParam String remark) {
        return toAjax(resourceRequestService.reject(requestId, remark));
    }

    @RequiresPermissions("gm:resource:edit")
    @Log(title = "资源申请", businessType = BusinessType.UPDATE)
    @PutMapping("/retry/{requestId}")
    public AjaxResult retry(@PathVariable Long requestId) {
        resourceRequestService.retry(requestId);
        GameResourceRequest r = resourceRequestService.selectById(requestId);
        AjaxResult res = success("重推完成");
        if (r != null) res.put("failedServerIds", r.getFailedServerIds());
        return res;
    }
}
