package com.github.athingx.athing.aliyun.config.component;

import com.github.athingx.athing.aliyun.config.api.Config;
import com.github.athingx.athing.aliyun.config.api.ConfigApplyListener;
import com.github.athingx.athing.aliyun.config.api.ConfigThingCom;
import com.github.athingx.athing.aliyun.config.api.Scope;
import com.github.athingx.athing.aliyun.config.component.domain.Meta;
import com.github.athingx.athing.aliyun.config.component.domain.Pull;
import com.github.athingx.athing.aliyun.config.component.domain.Push;
import com.github.athingx.athing.aliyun.config.component.util.GsonUtils;
import com.github.athingx.athing.aliyun.thing.runtime.ThingRuntime;
import com.github.athingx.athing.aliyun.thing.runtime.ThingRuntimes;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.ThingMessenger;
import com.github.athingx.athing.aliyun.thing.runtime.messenger.alink.ThingReplyImpl;
import com.github.athingx.athing.aliyun.thing.runtime.mqtt.ThingMqtt;
import com.github.athingx.athing.standard.thing.*;
import com.github.athingx.athing.standard.thing.boot.Initializing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.github.athingx.athing.aliyun.config.component.JsonSerializerImpl.serializer;
import static com.github.athingx.athing.aliyun.thing.runtime.messenger.alink.ThingReplyImpl.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 配置组件实现
 */
public class DefaultConfigThingCom implements ConfigThingCom, Initializing {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = GsonUtils.gson;
    private final ConfigOption option;

    private Thing thing;
    private ThingMqtt mqtt;
    private ThingExecutor executor;
    private ThingMessenger messenger;
    private volatile ConfigApplyListener listener;

    public DefaultConfigThingCom(ConfigOption option) {
        this.option = option;
    }

    @Override
    public void setConfigApplyListener(ConfigApplyListener listener) {
        this.listener = listener;
    }

    @Override
    public ThingFuture<Config> pull(Scope scope) {

        // 必须先完成初始化
        if (null == thing) {
            throw new IllegalStateException("not initialized!");
        }

        return executor.promise(promise -> {

            final ThingReplyFuture<Meta> callF = messenger.call(serializer, String.format("/sys/%s/%s/thing/config/get", thing.getProductId(), thing.getThingId()), Pull::new);
            callF.onFailure(promise::acceptFail)
                    .onSuccess(future -> {

                        final ThingReply<Meta> reply = future.getSuccess();

                        // 应答失败
                        if (!reply.isOk()) {
                            promise.tryException(new ThingException(thing, String.format("pull config failure! code=%s;message=%s;",
                                    reply.getCode(),
                                    reply.getMessage()
                            )));
                        }

                        // 应答成功
                        promise.trySuccess(new ConfigImpl(reply.getData(), executor, option));

                    });

        });
    }

    @Override
    public void onInitialized(Thing thing) throws Exception {
        final ThingRuntime runtime = ThingRuntimes.getThingRuntime(thing);
        Objects.requireNonNull(runtime, "runtime is required!");

        this.thing = thing;
        this.mqtt = runtime.getThingMqtt();
        this.executor = runtime.getThingExecutor();
        this.messenger = runtime.getThingMessenger();

        logger.info("{}/config init completed, connect-timeout={}ms;timeout={}ms",
                thing,
                option.getConnectTimeoutMs(),
                option.getTimeoutMs()
        );

        subscribePush().sync();
        subscribePullReply().sync();

    }

    /**
     * 订阅推送消息
     *
     * @return 订阅凭证
     */
    private ThingFuture<Void> subscribePush() {
        return mqtt.subscribe(String.format("/sys/%s/%s/thing/config/push", thing.getProductId(), thing.getThingId()), (topic, message) -> {

            final String rTopic = String.format("/sys/%s/%s/thing/config/push_reply", thing.getProductId(), thing.getThingId());
            final Push push = gson.fromJson(message.getStringData(UTF_8), Push.class);
            final Meta meta = push.getMeta();
            final String token = push.getToken();

            // 配置应用承诺
            final ThingPromise<Void> applyP = executor.promise(promise ->
                    promise.self()
                            .onFailure(future -> {
                                logger.warn("{}/config apply failure, version={}", thing, meta.getVersion(), future.getException());
                                messenger.post(serializer, rTopic, failure(token, ALINK_REPLY_PROCESS_ERROR, future.getException().getMessage()));
                            })
                            .onSuccess(future -> {
                                logger.info("{}/config apply success, version={}", thing, meta.getVersion());
                                messenger.post(serializer, rTopic, success(token));
                            }));

            // 配置应用履约
            executor.promise(applyP, promise -> {
                // 配置应用监听器
                final ConfigApplyListener listener = this.listener;
                if (null == listener) {
                    throw new RuntimeException("thing is not configurable, none listener found!");
                }

                // 应用配置
                listener.apply(token, new ConfigImpl(push.getMeta(), executor, option), new CommitterImpl(promise));
            });

        });
    }

    /**
     * 订阅拉取消息回复消息
     *
     * @return 订阅凭证
     */
    private ThingFuture<Void> subscribePullReply() {
        return mqtt.subscribe(String.format("/sys/%s/%s/thing/config/get_reply", thing.getProductId(), thing.getThingId()), (topic, message) -> {

            // 应答
            final ThingReply<Meta> reply = gson.fromJson(
                    message.getStringData(UTF_8),
                    new TypeToken<ThingReplyImpl<Meta>>() {
                    }.getType()
            );

            final String token = reply.getToken();
            final ThingPromise<ThingReply<Meta>> promise = messenger.reply(token);
            if (null == promise) {
                return;
            }
            promise.trySuccess(reply);

        });
    }

}
