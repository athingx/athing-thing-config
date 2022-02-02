package io.github.athingx.athing.aliyun.config.impl.domain;

import io.github.athingx.athing.aliyun.config.Scope;
import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.aliyun.thing.runtime.linker.TokenData;

/**
 * 配置拉取
 */
public class Pull implements TokenData {

    @SerializedName("id")
    private final String token;

    @SerializedName("version")
    private final String version;

    @SerializedName("method")
    private final String method;

    @SerializedName("params")
    private final Param param;

    public Pull(String token) {
        this.token = token;
        this.version = "1.0";
        this.method = "thing.config.get";
        this.param = new Param(Scope.PRODUCT.name(), "file");
    }

    @Override
    public String getToken() {
        return token;
    }

    private static class Param {

        @SerializedName("configScope")
        private final String scope;

        @SerializedName("getType")
        private final String type;

        private Param(String scope, String type) {
            this.scope = scope;
            this.type = type;
        }

    }

}
