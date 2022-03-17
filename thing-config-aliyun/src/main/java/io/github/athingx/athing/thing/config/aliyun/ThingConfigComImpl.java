package io.github.athingx.athing.thing.config.aliyun;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.aliyun.thing.runtime.ThingRuntime;
import io.github.athingx.athing.aliyun.thing.runtime.linker.LinkCaller;
import io.github.athingx.athing.aliyun.thing.runtime.linker.LinkOpReply;
import io.github.athingx.athing.aliyun.thing.runtime.linker.ThingLinker;
import io.github.athingx.athing.standard.thing.Thing;
import io.github.athingx.athing.standard.thing.ThingLifeCycle;
import io.github.athingx.athing.standard.thing.boot.Inject;
import io.github.athingx.athing.standard.thing.op.OpReply;
import io.github.athingx.athing.standard.thing.op.executor.ThingExecutor;
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
import static java.lang.String.format;

class ThingConfigComImpl implements ThingConfigCom, ThingLifeCycle {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = GsonUtils.gson;
    private final ConfigOption option;
    private final Set<ConfigListener> listeners = new ConcurrentHashMap<ConfigListener, Object>().keySet();

    @Inject
    private ThingRuntime runtime;

    private LinkCaller<Config> caller;

    public ThingConfigComImpl(ConfigOption option) {
        this.option = option;
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
        return caller.call(format("/sys/%s/thing/config/get", thing.path()), new Pull(token))
                .success(reply -> {
                    if (reply.isOk()) {
                        return reply.getData();
                    }
                    throw new Exception(format("fetch config failure! code=%s;message=%s;",
                            reply.getCode(),
                            reply.getDesc()
                    ));
                });
    }

    // 应用配置到配置监听器
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
        final ThingExecutor executor = thing.getThingOp().getThingExecutor();

        // 订阅配置推送：PUSH
        linker.subscribe(format("/sys/%s/thing/config/push", thing.path()), (topic, json) -> {

            final Push push = gson.fromJson(json, Push.class);
            final Meta meta = push.getMeta();
            final String token = push.getToken();

            linker.publish(topic + "_reply", replyForPushApply(thing, token, new ConfigImpl(meta, executor, option)))
                    .onSuccess(v -> logger.info("{}/config push reply success, token={};version={};", thing, token, meta.getVersion()))
                    .onFailure(e -> logger.warn("{}/config push reply failure, token={};version={};", thing, token, meta.getVersion(), e));

        }).sync();


        // 创建配置获取Call
        this.caller = linker.<Config>newCaller(format("/sys/%s/thing/config/get_reply", thing.path()), (rTopic, rJson) -> {

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
