package com.ruoyi.system.controller.game;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.GmMail;
import com.ruoyi.system.service.IGmMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GM 邮件Controller
 * <p>
 * 邮件管理 + 审核确认。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/game/gm/mail")
public class GmMailController extends BaseController {

    @Autowired
    private IGmMailService gmMailService;

    // ========== 管理接口 ==========

    /** 查询邮件列表 */
    @RequiresPermissions("gm:mail:list")
    @GetMapping("/list")
    public TableDataInfo list(GmMail mail) {
        startPage();
        List<GmMail> list = gmMailService.selectGmMailList(mail);
        return getDataTable(list);
    }

    /** 查询邮件详情 */
    @RequiresPermissions("gm:mail:list")
    @GetMapping("/{mailId}")
    public AjaxResult getInfo(@PathVariable Long mailId) {
        return success(gmMailService.selectGmMailById(mailId));
    }

    /** 新增邮件 */
    @RequiresPermissions("gm:mail:add")
    @Log(title = "GM邮件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GmMail mail) {
        mail.setCreatedBy(SecurityUtils.getUsername());
        return toAjax(gmMailService.insertGmMail(mail));
    }

    /** 修改邮件 */
    @RequiresPermissions("gm:mail:edit")
    @Log(title = "GM邮件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GmMail mail) {
        return toAjax(gmMailService.updateGmMail(mail));
    }

    /** 删除邮件 */
    @RequiresPermissions("gm:mail:remove")
    @Log(title = "GM邮件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{mailId}")
    public AjaxResult remove(@PathVariable Long mailId) {
        return toAjax(gmMailService.deleteGmMailById(mailId));
    }

    /** 提交审核 */
    @RequiresPermissions("gm:mail:edit")
    @Log(title = "GM邮件", businessType = BusinessType.UPDATE)
    @PutMapping("/submit/{mailId}")
    public AjaxResult submit(@PathVariable Long mailId) {
        return toAjax(gmMailService.submitForAudit(mailId));
    }

    // ========== 审核接口 ==========

    /** 待审核列表 */
    @RequiresPermissions("gm:mail:audit")
    @GetMapping("/audit/list")
    public TableDataInfo auditList(GmMail mail) {
        if (mail.getStatus() == null) {
            mail.setStatus(0); // 默认查待审核
        }
        startPage();
        List<GmMail> list = gmMailService.selectGmMailList(mail);
        return getDataTable(list);
    }

    /** 审核通过 */
    @RequiresPermissions("gm:mail:audit")
    @Log(title = "GM邮件审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/approve/{mailId}")
    public AjaxResult approve(@PathVariable Long mailId) {
        gmMailService.approveGmMail(mailId);
        GmMail mail = gmMailService.selectGmMailById(mailId);
        AjaxResult result = success("审核通过");
        if (mail != null) {
            result.put("mailId", mail.getMailId());
            result.put("status", mail.getStatus());
            result.put("failedServerIds", mail.getFailedServerIds());
        }
        return result;
    }

    /** 重推失败服务器 */
    @RequiresPermissions("gm:mail:edit")
    @Log(title = "GM邮件重推", businessType = BusinessType.UPDATE)
    @PutMapping("/retry/{mailId}")
    public AjaxResult retry(@PathVariable Long mailId) {
        gmMailService.retryGmMail(mailId);
        GmMail mail = gmMailService.selectGmMailById(mailId);
        AjaxResult result = success("重推完成");
        if (mail != null) {
            result.put("mailId", mail.getMailId());
            result.put("failedServerIds", mail.getFailedServerIds());
        }
        return result;
    }

    /** 审核驳回 */
    @RequiresPermissions("gm:mail:audit")
    @Log(title = "GM邮件审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/reject/{mailId}")
    public AjaxResult reject(@PathVariable Long mailId, @RequestParam String remark) {
        return toAjax(gmMailService.rejectGmMail(mailId, remark));
    }

}
