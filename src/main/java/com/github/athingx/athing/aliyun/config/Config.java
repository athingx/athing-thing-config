package com.github.athingx.athing.aliyun.config;

import com.github.athingx.athing.standard.thing.op.executor.ThingFuture;

/**
 * 设备配置
 */
public interface Config {

    /**
     * 获取配置版本号
     *
     * @return 配置版本号
     */
    String getVersion();

    /**
     * 获取配置范围
     *
     * @return 配置范围
     */
    Scope getScope();

    /**
     * 获取配置内容
     *
     * @return 获取凭证
     */
    ThingFuture<String> getContent();

}
