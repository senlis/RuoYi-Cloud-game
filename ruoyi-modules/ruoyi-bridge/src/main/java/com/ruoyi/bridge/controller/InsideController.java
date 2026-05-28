package com.ruoyi.bridge.controller;

import com.ruoyi.bridge.domain.dto.AuthRequest;
import com.ruoyi.bridge.domain.dto.AuthResponse;
import com.ruoyi.bridge.service.IBrUserService;
import com.ruoyi.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.web.domain.AjaxResult;

import java.util.Map;

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
public class InsideController {

    @Autowired
    private IBrUserService userService;

    private static final Logger log = LoggerFactory.getLogger(InsideController.class);

    @GetMapping("/health")
    public AjaxResult health() {
        return AjaxResult.success("bridge is running");
    }

    @PostMapping("/auth")
    public R<AuthResponse> auth(@RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = userService.loginOrCreate(authRequest);
        log.info("渠道认证成功:{}", authResponse);
        return R.ok(authResponse);
    }

    @PostMapping("/pay")
    public R<String> pay(@RequestParam Map<String,String> payparams){
        log.info("内部登录充值接口");
        return R.ok();
    }
}
