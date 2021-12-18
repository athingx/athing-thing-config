package com.github.athingx.athing.aliyun.config.thing.impl;

import com.github.athingx.athing.aliyun.config.thing.Config;
import com.github.athingx.athing.aliyun.config.thing.Scope;
import com.github.athingx.athing.aliyun.config.thing.impl.domain.Meta;
import com.github.athingx.athing.aliyun.config.thing.impl.util.HttpUtils;
import com.github.athingx.athing.aliyun.config.thing.impl.util.StringUtils;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.ThingException;
import com.github.athingx.athing.standard.thing.op.executor.ThingExecutor;
import com.github.athingx.athing.standard.thing.op.executor.ThingFuture;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

/**
 * 设备配置实现
 */
class ConfigImpl implements Config {

    private final Thing thing;
    private final Meta meta;
    private final Scope scope;
    private final ThingExecutor executor;
    private final ConfigOption option;
    private final AtomicReference<ThingFuture<String>> futureRef = new AtomicReference<>();

    public ConfigImpl(Thing thing, Meta meta, Scope scope, ThingExecutor executor, ConfigOption option) {
        this.thing = thing;
        this.meta = meta;
        this.scope = scope;
        this.executor = executor;
        this.option = option;
    }

    public ConfigImpl(Thing thing, Meta meta, ThingExecutor executor, ConfigOption option) {
        this(thing, meta, Scope.PRODUCT, executor, option);
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
                throw new ThingException(
                        thing,
                        format("get config: %s occur checksum failure, expect: %s but actual: %s",
                                getVersion(),
                                expect,
                                actual
                        )
                );
            }

            return content;
        });

        futureRef.set(future);
        return future;
    }

}
