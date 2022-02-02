package io.github.athingx.athing.aliyun.config.impl;

/**
 * 配置组件参数
 */
public class ConfigOption {

    /**
     * 下载配置连接超时时间
     */
    private long connectTimeoutMs = 1000L * 60;

    /**
     * 下载配置超时时间
     */
    private long timeoutMs = 3L * 1000 * 60;

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

}
