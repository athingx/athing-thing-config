package com.github.athingx.athing.aliyun.config.thing;

import com.github.athingx.athing.standard.api.ThingCom;
import com.github.athingx.athing.standard.thing.ThingException;
import com.github.athingx.athing.standard.thing.op.executor.ThingFuture;

/**
 * 配置组件
 */
public interface ConfigThingCom extends ThingCom {

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
     * 拉取最新配置
     *
     * @param scope 配置范围
     * @return 拉取存根
     */
    ThingFuture<Config> fetch(Scope scope);

    /**
     * 应用配置
     *
     * @param config 配置
     * @throws ThingException 应用失败
     */
    void apply(Config config) throws ThingException;

}
