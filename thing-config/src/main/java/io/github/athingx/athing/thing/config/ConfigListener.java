package io.github.athingx.athing.thing.config;

/**
 * 配置监听器
 */
public interface ConfigListener {

    /**
     * 应用配置
     *
     * @param config 配置
     * @throws Exception 配置应用失败
     */
    void apply(Config config) throws Exception;

}
