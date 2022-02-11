package io.github.athingx.athing.thing.config.aliyun;

import io.github.athingx.athing.thing.config.Config;
import io.github.athingx.athing.thing.config.Scope;
import io.github.athingx.athing.thing.config.aliyun.domain.Meta;
import io.github.athingx.athing.thing.config.aliyun.util.HttpUtils;
import io.github.athingx.athing.thing.config.aliyun.util.StringUtils;
import io.github.oldmanpushcart.jpromisor.ListenableFuture;
import io.github.oldmanpushcart.jpromisor.Promisor;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

/**
 * 设备配置实现
 */
class ConfigImpl implements Config {

    private final Meta meta;
    private final Scope scope;
    private final Executor executor;
    private final ConfigOption option;
    private final AtomicReference<ListenableFuture<String>> futureRef = new AtomicReference<>();

    public ConfigImpl(Meta meta, Scope scope, Executor executor, ConfigOption option) {
        this.meta = meta;
        this.scope = scope;
        this.executor = executor;
        this.option = option;
    }

    public ConfigImpl(Meta meta, Executor executor, ConfigOption option) {
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
    public synchronized ListenableFuture<String> getContent() {
        // 命中缓存，直接从缓存返回
        if (futureRef.get() != null) {
            return futureRef.get();
        }

        final ListenableFuture<String> future = new Promisor().fulfill(executor, () -> {
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
                throw new Exception(
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
