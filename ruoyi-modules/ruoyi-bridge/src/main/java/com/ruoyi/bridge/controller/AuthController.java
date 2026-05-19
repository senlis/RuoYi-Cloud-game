package com.ruoyi.bridge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;

/**
 * 认证入口
 * <p>
 * 各渠道的认证Controller独立注册，路径自行定义，例如：
 * /bridge/auth/huawei/login  → HuaweiAuthController
 * /bridge/auth/oppo/login   → OppoAuthController
 * <p>
 * 本Controller仅提供健康检查。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/bridge")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/health")
    public AjaxResult health() {
        return AjaxResult.success("bridge is running");
    }
}
