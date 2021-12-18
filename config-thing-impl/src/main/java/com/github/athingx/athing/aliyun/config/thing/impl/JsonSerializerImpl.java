package com.github.athingx.athing.aliyun.config.thing.impl;

import com.github.athingx.athing.aliyun.config.thing.impl.util.GsonUtils;
import com.github.athingx.athing.aliyun.thing.runtime.linker.JsonSerializer;

class JsonSerializerImpl implements JsonSerializer {

    public static final JsonSerializer serializer = new JsonSerializerImpl();

    @Override
    public String toJson(Object object) {
        return GsonUtils.gson.toJson(object);
    }

}
