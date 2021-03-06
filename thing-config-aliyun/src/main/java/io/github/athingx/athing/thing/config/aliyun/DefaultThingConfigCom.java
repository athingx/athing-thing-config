package io.github.athingx.athing.thing.config.aliyun;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.aliyun.thing.runtime.ThingRuntime;
import io.github.athingx.athing.aliyun.thing.runtime.linker.LinkCaller;
import io.github.athingx.athing.aliyun.thing.runtime.linker.LinkOpReply;
import io.github.athingx.athing.aliyun.thing.runtime.linker.ThingLinker;
import io.github.athingx.athing.standard.thing.Thing;
import io.github.athingx.athing.standard.thing.ThingComListener;
import io.github.athingx.athing.standard.thing.op.OpReply;
import io.github.athingx.athing.standard.thing.executor.ThingExecutor;
import io.github.athingx.athing.thing.config.Config;
import io.github.athingx.athing.thing.config.ConfigListener;
import io.github.athingx.athing.thing.config.Scope;
import io.github.athingx.athing.thing.config.ThingConfigCom;
import io.github.athingx.athing.thing.config.aliyun.domain.Meta;
import io.github.athingx.athing.thing.config.aliyun.domain.Pull;
import io.github.athingx.athing.thing.config.aliyun.domain.Push;
import io.github.athingx.athing.thing.config.aliyun.util.GsonUtils;
import io.github.oldmanpushcart.jpromisor.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.athingx.athing.aliyun.thing.runtime.linker.LinkOpReply.ALINK_REPLY_PROCESS_ERROR;
import static java.lang.Integer.parseInt;

public class DefaultThingConfigCom implements ThingConfigCom, ThingComListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = GsonUtils.gson;
    private final ConfigOption option;
    private final ThingRuntime runtime;
    private final Set<ConfigListener> listeners = ConcurrentHashMap.newKeySet();

    private LinkCaller<Config> caller;

    public DefaultThingConfigCom(Thing thing, ConfigOption option) {
        this.option = option;
        this.runtime = ThingRuntime.getInstance(thing);
    }

    @Override
    public void appendListener(ConfigListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ConfigListener listener) {
        listeners.remove(listener);
    }

    @Override
    public ListenableFuture<Config> update(Scope scope) {
        return fetch(scope).success(config -> {
            apply(config);
            return config;
        });
    }

    @Override
    public ListenableFuture<Config> fetch(Scope scope) {
        final Thing thing = runtime.getThing();
        final ThingLinker linker = runtime.getThingLinker();
        final String token = linker.generateToken();
        return caller.call("/sys/%s/thing/config/get".formatted(thing.getPath()), new Pull(token))
                .success(reply -> {
                    if (reply.isOk()) {
                        return reply.getData();
                    }
                    throw new Exception("fetch config failure! code=%s;message=%s;".formatted(
                            reply.getCode(),
                            reply.getDesc()
                    ));
                });
    }

    // ??????????????????????????????
    private void apply(Config config) throws Exception {
        for (final ConfigListener listener : listeners) {
            listener.apply(config);
        }
    }

    private LinkOpReply<Void> replyForPushApply(Thing thing, String token, Config config) {
        if (listeners.isEmpty()) {
            logger.warn("{}/config give up: none-listener, token={};version={};", thing, token, config.getVersion());
            return LinkOpReply.failure(token, ALINK_REPLY_PROCESS_ERROR, "none-listener");
        }
        try {
            apply(config);
            logger.info("{}/config push apply success, token={};version={};", thing, token, config.getVersion());
            return LinkOpReply.success(token);
        } catch (Exception cause) {
            logger.warn("{}/config push apply occur error, token={};version={};", thing, token, config.getVersion(), cause);
            return LinkOpReply.failure(token, ALINK_REPLY_PROCESS_ERROR, cause.getLocalizedMessage());
        }
    }

    @Override
    public void onLoaded(Thing thing) throws Exception {

        final ThingLinker linker = runtime.getThingLinker();
        final ThingExecutor executor = thing.getExecutor();

        // ?????????????????????PUSH
        linker.subscribe("/sys/%s/thing/config/push".formatted(thing.getPath()), (topic, json) -> {

            final Push push = gson.fromJson(json, Push.class);
            final Meta meta = push.getMeta();
            final String token = push.getToken();

            linker.publish(topic + "_reply", replyForPushApply(thing, token, new ConfigImpl(meta, executor, option)))
                    .onSuccess(v -> logger.info("{}/config push reply success, token={};version={};", thing, token, meta.getVersion()))
                    .onFailure(e -> logger.warn("{}/config push reply failure, token={};version={};", thing, token, meta.getVersion(), e));

        }).sync();


        // ??????????????????Call
        this.caller = linker.<Config>newCaller("/sys/%s/thing/config/get_reply".formatted(thing.getPath()), (rTopic, rJson) -> {

            final OpReply<Meta> reply = gson.fromJson(
                    rJson,
                    new TypeToken<LinkOpReply<Meta>>() {
                    }.getType()
            );

            return reply.isOk()
                    ? LinkOpReply.success(reply.getToken(), new ConfigImpl(reply.getData(), executor, option))
                    : LinkOpReply.failure(reply.getToken(), parseInt(reply.getCode()), reply.getDesc());

        }).get();

    }

}
