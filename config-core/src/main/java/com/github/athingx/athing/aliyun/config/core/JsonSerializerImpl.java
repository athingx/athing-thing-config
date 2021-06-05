package com.github.athingx.athing.aliyun.config.core;

import com.github.athingx.athing.aliyun.config.core.util.GsonUtils;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.JsonSerializer;

class JsonSerializerImpl implements JsonSerializer {

    public static final JsonSerializer serializer = new JsonSerializerImpl();

    @Override
    public String toJson(Object object) {
        return GsonUtils.gson.toJson(object);
    }

}
