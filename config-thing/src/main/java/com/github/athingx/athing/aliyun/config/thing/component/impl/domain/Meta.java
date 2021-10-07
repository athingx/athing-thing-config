package com.github.athingx.athing.aliyun.config.thing.component.impl.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 配置元数据
 */
public class Meta {

    @SerializedName("configId")
    private String version;

    @SerializedName("sign")
    private String configCHS;

    @SerializedName("url")
    private String configURL;

    /**
     * 配置版本
     *
     * @return 配置版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 配置校验值
     *
     * @return 配置校验值
     */
    public String getConfigCHS() {
        return configCHS;
    }

    /**
     * 配置下载地址
     *
     * @return 配置下载地址
     */
    public String getConfigURL() {
        return configURL;
    }

}
