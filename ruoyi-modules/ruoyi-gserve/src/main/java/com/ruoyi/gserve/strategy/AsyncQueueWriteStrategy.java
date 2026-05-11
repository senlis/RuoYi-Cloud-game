package com.ruoyi.gserve.strategy;

import com.ruoyi.gserve.config.GserveConfig;
import com.ruoyi.gserve.strategy.model.BufferedEvent;
import com.ruoyi.gserve.service.ClickHouseWriterService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步内存队列写入策略
 * <p>
 * 游戏服上报后事件先进入内存队列立即返回，后台定时批量写入 ClickHouse。
 * 适合高并发场景，削峰填谷，写入效率高。
 * <p>
 * 配置（Nacos game.analytics.etl）：
 * - write-strategy: async          ← 启用异步策略
 * - async-queue.capacity: 50000    队列容量
 * - async-queue.flush-interval-ms: 3000  定时刷新间隔
 * - async-queue.flush-batch-size: 5000    每批最大行数
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "game.analytics.etl.write-strategy", havingValue = "async")
public class AsyncQueueWriteStrategy implements EventWriteStrategy {

    private static final Logger log = LoggerFactory.getLogger(AsyncQueueWriteStrategy.class);

    private final ClickHouseWriterService clickHouseWriterService;
    private final GserveConfig gserveConfig;

    /** 内存队列（阻塞队列，防止生产者过快） */
    private BlockingQueue<BufferedEvent> queue;

    /** 定时调度器 */
    private ScheduledExecutorService scheduler;

    /** 累计入队计数 */
    private final AtomicLong enqueued = new AtomicLong(0);
    /** 累计写入计数 */
    private final AtomicLong written = new AtomicLong(0);
    /** 累计丢弃计数（队列满） */
    private final AtomicLong dropped = new AtomicLong(0);

    public AsyncQueueWriteStrategy(ClickHouseWriterService clickHouseWriterService,
                                    GserveConfig gserveConfig) {
        this.clickHouseWriterService = clickHouseWriterService;
        this.gserveConfig = gserveConfig;
    }

    @PostConstruct
    public void init() {
        int capacity = gserveConfig.getAsyncQueue().getCapacity();
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "event-flusher");
            t.setDaemon(true);
            return t;
        });

        long interval = gserveConfig.getAsyncQueue().getFlushIntervalMs();
        scheduler.scheduleWithFixedDelay(this::flush, interval, interval, TimeUnit.MILLISECONDS);
        log.info("异步事件队列初始化: capacity={}, flushInterval={}ms", capacity, interval);
    }

    @PreDestroy
    public void destroy() {
        log.info("异步事件队列关闭中... enqueued={}, written={}, dropped={}",
                enqueued.get(), written.get(), dropped.get());
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 关闭前强制刷空队列
        flush();
        log.info("异步事件队列已关闭, 最终写入: {}", written.get());
    }

    @Override
    public int write(long projectId, String tableName, List<String> columns, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return 0;
        int count = 0;
        for (Map<String, Object> row : rows) {
            BufferedEvent event = new BufferedEvent(projectId, tableName, columns, row);
            if (queue.offer(event)) {
                count++;
                enqueued.incrementAndGet();
            } else {
                dropped.incrementAndGet();
            }
        }
        if (count < rows.size()) {
            log.warn("事件队列满, 已丢弃 {} 条(入队{}/{})", rows.size() - count, count, rows.size());
        }
        return count;
    }

    @Override
    public String name() {
        return "async";
    }

    /**
     * 定时刷新：按 projectId + tableName 分组，批量写入 ClickHouse
     */
    public void flush() {
        List<BufferedEvent> batch = new ArrayList<>();
        int batchSize = gserveConfig.getAsyncQueue().getFlushBatchSize();
        queue.drainTo(batch, batchSize);
        if (batch.isEmpty()) return;

        // 按 (projectId, tableName) 分组
        Map<String, List<BufferedEvent>> groups = new LinkedHashMap<>();
        for (BufferedEvent e : batch) {
            String key = e.getProjectId() + "|" + e.getTableName();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        int total = 0;
        for (Map.Entry<String, List<BufferedEvent>> entry : groups.entrySet()) {
            List<BufferedEvent> events = entry.getValue();
            long projectId = events.get(0).getProjectId();
            String tableName = events.get(0).getTableName();

            // 合并列和行
            Set<String> colSet = new LinkedHashSet<>();
            for (BufferedEvent e : events) {
                colSet.addAll(e.getColumns());
            }
            List<String> columns = new ArrayList<>(colSet);

            List<Map<String, Object>> rows = new ArrayList<>();
            for (BufferedEvent e : events) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String col : columns) {
                    row.put(col, e.getRow().get(col));
                }
                rows.add(row);
            }

            try {
                int n = clickHouseWriterService.batchWrite(projectId, tableName, columns, rows);
                total += n;
                written.addAndGet(n);
                log.debug("异步队列写入: table={}, rows={}, total={}", tableName, n, written.get());
            } catch (Exception e) {
                log.error("异步队列写入失败: table={}, rows={}", tableName, events.size(), e);
                // TODO: 写入失败可投递到死信队列/错误表
            }
        }

        if (log.isDebugEnabled() && total > 0) {
            log.debug("异步队列刷新完成: batch={}, written={}, totalWritten={}", batch.size(), total, written.get());
        }
    }

    /** 获取队列积压量 */
    public int getBacklog() {
        return queue.size();
    }

    /** 获取统计信息 */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("enqueued", enqueued.get());
        stats.put("written", written.get());
        stats.put("dropped", dropped.get());
        stats.put("backlog", queue.size());
        stats.put("capacity", gserveConfig.getAsyncQueue().getCapacity());
        return stats;
    }
}
