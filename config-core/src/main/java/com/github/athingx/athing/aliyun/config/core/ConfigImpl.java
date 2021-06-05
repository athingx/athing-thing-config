package com.github.athingx.athing.aliyun.config.core;

import com.github.athingx.athing.aliyun.config.api.Config;
import com.github.athingx.athing.aliyun.config.api.Scope;
import com.github.athingx.athing.aliyun.config.core.domain.Meta;
import com.github.athingx.athing.aliyun.config.core.util.HttpUtils;
import com.github.athingx.athing.aliyun.config.core.util.StringUtils;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.athingx.athing.standard.thing.ThingFuture;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

/**
 * 设备配置实现
 */
class ConfigImpl implements Config {

    private final Meta meta;
    private final Scope scope;
    private final ThingExecutor executor;
    private final ConfigOption option;
    private final AtomicReference<ThingFuture<String>> futureRef = new AtomicReference<>();

    public ConfigImpl(Meta meta, Scope scope, ThingExecutor executor, ConfigOption option) {
        this.meta = meta;
        this.scope = scope;
        this.executor = executor;
        this.option = option;
    }

    public ConfigImpl(Meta meta, ThingExecutor executor, ConfigOption option) {
        this(meta, Scope.PRODUCT, executor, option);
    }

    @Override
    public String getVersion() {
        return meta.getVersion();
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public synchronized ThingFuture<String> getContent() {
        // 命中缓存，直接从缓存返回
        if (futureRef.get() != null) {
            return futureRef.get();
        }

        final ThingFuture<String> future = executor.submit(() -> {

            // 获取配置文件内容
            final String content = HttpUtils.getAsString(
                    new URL(meta.getConfigURL()),
                    option.getConnectTimeoutMs(),
                    option.getTimeoutMs()
            );

            // 校验获取的配置文件内容
            final String expect = meta.getConfigCHS().toUpperCase();
            final String actual = StringUtils.signBySHA256(content).toUpperCase();
            if (!Objects.equals(expect, actual)) {
                throw new RuntimeException(format("checksum failure, expect: %s but actual: %s", expect, actual));
            }

            return content;
        });

        futureRef.set(future);
        return future;
    }

}
