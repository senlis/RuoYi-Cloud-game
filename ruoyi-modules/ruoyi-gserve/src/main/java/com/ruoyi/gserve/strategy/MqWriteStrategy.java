package com.ruoyi.gserve.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MQ 写入策略（预留）
 * <p>
 * 将事件数据发送到消息队列（RocketMQ / Kafka），由消费者异步写入 ClickHouse。
 * 当前为空实现，待后续接入 MQ 后补全。
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "game.analytics.etl.write-strategy", havingValue = "mq")
public class MqWriteStrategy implements EventWriteStrategy {

    private static final Logger log = LoggerFactory.getLogger(MqWriteStrategy.class);

    @Override
    public int write(long projectId, String tableName, List<String> columns, List<Map<String, Object>> rows) {
        log.warn("MQ 写入策略尚未实现，丢弃 {} 条事件: projectId={}, table={}",
                rows != null ? rows.size() : 0, projectId, tableName);
        return 0;
    }

    @Override
    public String name() {
        return "mq";
    }
}
