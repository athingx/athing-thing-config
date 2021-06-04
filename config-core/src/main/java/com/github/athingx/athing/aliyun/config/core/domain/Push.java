package com.github.athingx.athing.aliyun.config.core.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 推送配置
 */
public class Push {

    @SerializedName("id")
    private String token;

    @SerializedName("params")
    private Meta meta;

    /**
     * 获取推送令牌
     *
     * @return 推送令牌
     */
    public String getToken() {
        return token;
    }

    /**
     * 获取配置元数据
     *
     * @return 配置元数据
     */
    public Meta getMeta() {
        return meta;
    }

}
