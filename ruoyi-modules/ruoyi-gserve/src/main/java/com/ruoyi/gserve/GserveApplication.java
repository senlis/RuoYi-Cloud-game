package com.ruoyi.gserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 游戏数据接收服务 (gserve)
 * <p>
 * 职责：接收游戏服 HTTP 上报的行为日志，校验签名，批量写入 ClickHouse
 * 独立可部署，支持多实例水平扩展
 *
 * @author ruoyi
 */
@SpringBootApplication
public class GserveApplication {

    public static void main(String[] args) {
        SpringApplication.run(GserveApplication.class, args);
        System.out.println(">>>> gserve 数据接收服务启动成功 <<<<");
    }
}
