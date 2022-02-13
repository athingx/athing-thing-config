package io.github.athingx.athing.thing.config;

import io.github.athingx.athing.standard.component.ThingCom;
import io.github.oldmanpushcart.jpromisor.ListenableFuture;

/**
 * 配置组件
 */
public interface ThingConfigCom extends ThingCom {

    /**
     * 添加监听器
     *
     * @param listener 配置监听器
     */
    void appendListener(ConfigListener listener);

    /**
     * 移除监听器
     *
     * @param listener 配置监听器
     */
    void removeListener(ConfigListener listener);

    /**
     * 更新最新配置
     *
     * @param scope 配置范围
     * @return 更新Future
     */
    ListenableFuture<Config> update(Scope scope);

    /**
     * 拉取最新配置
     *
     * @param scope 配置范围
     * @return 配置Future
     */
    ListenableFuture<Config> fetch(Scope scope);

}
