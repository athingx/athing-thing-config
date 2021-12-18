package com.github.athingx.athing.aliyun.config.thing.impl;

import com.github.athingx.athing.aliyun.config.thing.Config;
import com.github.athingx.athing.aliyun.config.thing.ConfigListener;
import com.github.athingx.athing.aliyun.config.thing.ConfigThingCom;
import com.github.athingx.athing.aliyun.config.thing.Scope;
import com.github.athingx.athing.standard.thing.ThingException;
import com.github.athingx.athing.standard.thing.op.executor.ThingFuture;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigThingComTestCase extends ThingSupport {

    @Test
    public void test$thing$config$fetch$success() throws Exception {

        final ThingFuture<Config> fetchF = thing.getThingCom(ConfigThingCom.class)
                .fetch(Scope.PRODUCT);

        fetchF.awaitUninterruptible();
        final Config config = fetchF.getSuccess();
        assertConfig(config);

    }

    @Test
    public void test$thing$config$apply$success() throws Exception {
        final ConfigThingCom configThingCom = thing.getThingCom(ConfigThingCom.class);
        final AtomicBoolean flag = new AtomicBoolean(false);
        final ConfigListener listener = config -> {
            assertConfig(config);
            flag.set(true);
        };

        try {
            configThingCom.appendListener(listener);
            configThingCom.apply(configThingCom.fetch(Scope.PRODUCT).awaitUninterruptible().getSuccess());
            Assert.assertTrue(flag.get());
        } finally {
            configThingCom.removeListener(listener);
        }

    }

    @Test(expected = ThingException.class)
    public void test$thing$config$apply$exception() throws Exception {
        final ConfigThingCom configThingCom = thing.getThingCom(ConfigThingCom.class);
        final AtomicBoolean flag = new AtomicBoolean(false);
        final ConfigListener listener = config -> {
            assertConfig(config);
            flag.set(true);
            throw new RuntimeException("test");
        };

        try {
            configThingCom.appendListener(listener);
            configThingCom.apply(configThingCom.fetch(Scope.PRODUCT).awaitUninterruptible().getSuccess());
        } catch (ThingException cause) {
            Assert.assertEquals("test", cause.getCause().getMessage());
            Assert.assertTrue(flag.get());
            throw cause;
        } finally {
            configThingCom.removeListener(listener);
        }

    }

    private void assertConfig(Config config) {
        Assert.assertNotNull(config);
        Assert.assertNotNull(config.getVersion());
        Assert.assertNotNull(config.getScope());
        Assert.assertNotNull(config.getContent());
    }

}
