package com.ruoyi.system.controller.bridge;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.BrPayOrder;
import com.ruoyi.system.service.bridge.IBrPayOrderService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付订单Controller（管理端）
 * <p>
 * 查询渠道分库中的 br_pay_order 表数据，提供列表查询和Excel导出功能。
 *
 * @author ruoyi
 */
@Slf4j
@RestController
@RequestMapping("/game/bridge/payOrder")
public class BrPayOrderController extends BaseController {

    @Autowired
    private IBrPayOrderService brPayOrderService;

    /**
     * 查询支付订单列表
     *
     * @param channelKey 渠道KEY（必填，用于路由到对应渠道数据库）
     * @param payOrder   查询条件
     * @return 分页结果
     */
    @RequiresPermissions("bridge:payOrder:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam String channelKey, BrPayOrder payOrder) {
        payOrder.setChannelKey(channelKey);
        startPage();
        List<BrPayOrder> list = brPayOrderService.selectPayOrderList(channelKey, payOrder);
        return getDataTable(list);
    }

    /**
     * 导出支付订单列表
     *
     * @param response   HTTP响应
     * @param channelKey 渠道KEY（必填，用于路由到对应渠道数据库）
     * @param payOrder   查询条件
     */
    @RequiresPermissions("bridge:payOrder:export")
    @Log(title = "支付订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestParam String channelKey, BrPayOrder payOrder) {
        payOrder.setChannelKey(channelKey);
        List<BrPayOrder> list = brPayOrderService.selectPayOrderList(channelKey, payOrder);
        ExcelUtil<BrPayOrder> util = new ExcelUtil<>(BrPayOrder.class);
        util.exportExcel(response, list, "支付订单");
    }
}
