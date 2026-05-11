package com.ruoyi.gserve.strategy;

import com.ruoyi.gserve.service.ClickHouseWriterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 同步直写策略（默认）
 * <p>
 * 游戏服上报后同步写入 ClickHouse，写入完成才返回。
 * 适合低并发场景，数据量小时延迟最低。
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "game.analytics.etl.write-strategy", havingValue = "direct", matchIfMissing = true)
public class DirectWriteStrategy implements EventWriteStrategy {

    private static final Logger log = LoggerFactory.getLogger(DirectWriteStrategy.class);

    private final ClickHouseWriterService clickHouseWriterService;

    public DirectWriteStrategy(ClickHouseWriterService clickHouseWriterService) {
        this.clickHouseWriterService = clickHouseWriterService;
    }

    @Override
    public int write(long projectId, String tableName, List<String> columns, List<Map<String, Object>> rows) {
        return clickHouseWriterService.batchWrite(projectId, tableName, columns, rows);
    }

    @Override
    public String name() {
        return "direct";
    }
}
