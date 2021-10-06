package com.github.athingx.athing.aliyun.config;

import com.github.athingx.athing.standard.api.ThingCom;
import com.github.athingx.athing.standard.thing.op.executor.ThingFuture;

/**
 * 配置组件
 */
public interface ConfigThingCom extends ThingCom {

    /**
     * 设置配置应用监听器
     *
     * @param listener 配置应用监听器
     */
    void setConfigApplyListener(ConfigApplyListener listener);

    /**
     * 拉取最新配置
     *
     * @param scope 配置范围
     * @return 拉取存根
     */
    ThingFuture<Config> pull(Scope scope);

}
