package com.github.athingx.athing.aliyun.config.thing.impl;

import com.github.athingx.athing.aliyun.config.thing.Config;
import com.github.athingx.athing.aliyun.config.thing.ConfigListener;
import com.github.athingx.athing.aliyun.config.thing.ConfigThingCom;
import com.github.athingx.athing.aliyun.config.thing.Scope;
import com.github.athingx.athing.aliyun.config.thing.impl.domain.Meta;
import com.github.athingx.athing.aliyun.config.thing.impl.domain.Pull;
import com.github.athingx.athing.aliyun.config.thing.impl.domain.Push;
import com.github.athingx.athing.aliyun.config.thing.impl.util.GsonUtils;
import com.github.athingx.athing.aliyun.thing.runtime.ThingRuntime;
import com.github.athingx.athing.aliyun.thing.runtime.linker.ThingLinker;
import com.github.athingx.athing.aliyun.thing.runtime.linker.impl.ThingReplyImpl;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.athingx.athing.standard.api.annotation.ThComInject;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.ThingException;
import com.github.athingx.athing.standard.thing.boot.Initializing;
import com.github.athingx.athing.standard.thing.op.ThingReply;
import com.github.athingx.athing.standard.thing.op.ThingReplyFuture;
import com.github.athingx.athing.standard.thing.op.executor.Fulfill;
import com.github.athingx.athing.standard.thing.op.executor.ThingExecutor;
import com.github.athingx.athing.standard.thing.op.executor.ThingFuture;
import com.github.athingx.athing.standard.thing.op.executor.ThingPromise;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.athingx.athing.aliyun.config.thing.impl.JsonSerializerImpl.serializer;
import static com.github.athingx.athing.aliyun.thing.runtime.linker.impl.ThingReplyImpl.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 配置组件实现
 */
public class ConfigThingComImpl implements ConfigThingCom, Initializing {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = GsonUtils.gson;
    private final ConfigOption option;
    private final Set<ConfigListener> listeners = new LinkedHashSet<>();

    @ThComInject
    private ThingRuntime runtime;

    public ConfigThingComImpl(ConfigOption option) {
        this.option = option;
    }

    @Override
    public void appendListener(ConfigListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(ConfigListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private ThingReplyFuture<Meta> pull(Thing thing) {
        return runtime.getThingLinker().call(
                serializer,
                String.format("/sys/%s/%s/thing/config/get", thing.getProductId(), thing.getThingId()),
                Pull::new
        );
    }

    @Override
    public ThingFuture<Config> fetch(Scope scope) {

        final Thing thing = runtime.getThing();
        final ThingExecutor executor = thing.getThingOp().getThingExecutor();

        return executor.promise(fetchP -> pull(thing)
                .onFailure(fetchP::acceptFail)
                .onSuccess(pullF -> {

                    final ThingReply<Meta> reply = pullF.getSuccess();

                    // 应答失败
                    if (!reply.isOk()) {
                        fetchP.tryException(new ThingException(thing, String.format("pull config failure! code=%s;message=%s;",
                                reply.getCode(),
                                reply.getMessage()
                        )));
                    }

                    // 应答成功
                    else {
                        fetchP.trySuccess(new ConfigImpl(thing, reply.getData(), executor, option));
                    }

                }));
    }

    @Override
    public void apply(Config config) throws ThingException {

        // 先克隆一份，避免阻塞
        final Set<ConfigListener> clones;
        synchronized (listeners) {
            clones = new LinkedHashSet<>(listeners);
        }

        // 应用配置
        for (final ConfigListener listener : clones) {
            try {
                listener.apply(config);
            } catch (Exception cause) {
                throw new ThingException(
                        runtime.getThing(),
                        String.format("apply config occur error at listener: %s", listener),
                        cause
                );
            }
        }

    }

    @Override
    public void onInitialized(Thing thing) throws Exception {
        assert thing == runtime.getThing() : "thing not match!";
        subscribePush().sync();
        subscribePullReply().sync();
        logger.info("{}/config init completed, connect-timeout={}ms;timeout={}ms",
                thing,
                option.getConnectTimeoutMs(),
                option.getTimeoutMs()
        );
    }


    /**
     * 订阅推送消息
     *
     * @return 订阅凭证
     */
    private ThingFuture<Void> subscribePush() {

        final Thing thing = runtime.getThing();
        final ThingExecutor executor = thing.getThingOp().getThingExecutor();
        final ThingLinker linker = runtime.getThingLinker();
        final ThingMqtt mqtt = runtime.getThingMqtt();

        return mqtt.subscribe(String.format("/sys/%s/%s/thing/config/push", thing.getProductId(), thing.getThingId()), (topic, message) -> {

            final String rTopic = topic + "_reply";
            final Push push = gson.fromJson(message.getStringData(UTF_8), Push.class);
            final Meta meta = push.getMeta();
            final String token = push.getToken();
            final Config config = new ConfigImpl(thing, push.getMeta(), executor, option);

            executor.promise((Fulfill<Void>) pushP -> apply(config))
                    .self()
                    .onFailure(pushF -> {
                        logger.warn("{}/config apply failure, version={}", thing, meta.getVersion(), pushF.getException());
                        linker.post(serializer, rTopic, failure(token, ALINK_REPLY_PROCESS_ERROR, pushF.getException().getMessage()));
                    })
                    .onSuccess(pushF -> {
                        logger.info("{}/config apply success, version={}", thing, meta.getVersion());
                        linker.post(serializer, rTopic, success(token));
                    });

        });
    }

    /**
     * 订阅拉取消息回复消息
     *
     * @return 订阅凭证
     */
    private ThingFuture<Void> subscribePullReply() {

        final Thing thing = runtime.getThing();
        final ThingLinker linker = runtime.getThingLinker();
        final ThingMqtt mqtt = runtime.getThingMqtt();

        return mqtt.subscribe(String.format("/sys/%s/%s/thing/config/get_reply", thing.getProductId(), thing.getThingId()), (topic, message) -> {

            // 应答
            final ThingReply<Meta> reply = gson.fromJson(
                    message.getStringData(UTF_8),
                    new TypeToken<ThingReplyImpl<Meta>>() {
                    }.getType()
            );

            final String token = reply.getToken();
            final ThingPromise<ThingReply<Meta>> promise = linker.reply(token);

            // 如果应答没有找到发起的promise存根，说明应答已经超时或者设备已经被重启，应该放弃对这个应答的处理
            if (null == promise) {
                logger.warn("{}/config receive config get-reply but none promise found", thing);
                return;
            }
            promise.trySuccess(reply);

        });
    }

}
