package com.ruoyi.gserve.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * gserve 服务配置
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "game.analytics.etl")
public class GserveConfig {

    /** 单次最大接收条数 */
    private int maxBatchSize = 5000;

    /** ClickHouse 写入批次大小 */
    private int defaultBatchSize = 1000;

    /** 写入线程数 */
    private int threadPool = 4;

    /** 重试配置 */
    private RetryConfig retry = new RetryConfig();

    /** 错误日志配置 */
    private ErrorLogConfig errorLog = new ErrorLogConfig();

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getDefaultBatchSize() {
        return defaultBatchSize;
    }

    public void setDefaultBatchSize(int defaultBatchSize) {
        this.defaultBatchSize = defaultBatchSize;
    }

    public int getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(int threadPool) {
        this.threadPool = threadPool;
    }

    public RetryConfig getRetry() {
        return retry;
    }

    public void setRetry(RetryConfig retry) {
        this.retry = retry;
    }

    public ErrorLogConfig getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(ErrorLogConfig errorLog) {
        this.errorLog = errorLog;
    }

    public static class RetryConfig {
        private int maxRetries = 3;
        private long retryIntervalMs = 1000;

        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        public long getRetryIntervalMs() { return retryIntervalMs; }
        public void setRetryIntervalMs(long retryIntervalMs) { this.retryIntervalMs = retryIntervalMs; }
    }

    public static class ErrorLogConfig {
        private boolean enabled = true;
        private int maxRetentionDays = 30;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxRetentionDays() { return maxRetentionDays; }
        public void setMaxRetentionDays(int maxRetentionDays) { this.maxRetentionDays = maxRetentionDays; }
    }
}
