package com.github.athingx.athing.aliyun.config.core.domain;

import com.github.athingx.athing.aliyun.config.api.Scope;
import com.google.gson.annotations.SerializedName;

/**
 * 配置拉取
 */
public class Pull {

    @SerializedName("id")
    private String token;

    @SerializedName("version")
    private String version;

    @SerializedName("method")
    private String method;

    @SerializedName("params")
    private Param param;

    public Pull(String token) {
        this.token = token;
        this.version = "1.0";
        this.method = "thing.config.get";
        this.param = new Param(Scope.PRODUCT.name(), "file");
    }

    private static class Param {

        @SerializedName("configScope")
        private String scope;

        @SerializedName("getType")
        private String type;

        private Param(String scope, String type) {
            this.scope = scope;
            this.type = type;
        }

    }

}
