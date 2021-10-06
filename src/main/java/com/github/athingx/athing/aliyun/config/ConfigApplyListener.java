package com.github.athingx.athing.aliyun.config;

/**
 * 配置应用监听器
 */
public interface ConfigApplyListener {

    /**
     * 应用配置
     *
     * @param token     令牌（平台推送）
     * @param config    配置
     * @param committer 提交器
     * @throws Exception 配置应用失败
     */
    void apply(String token, Config config, Committer committer) throws Exception;

}
