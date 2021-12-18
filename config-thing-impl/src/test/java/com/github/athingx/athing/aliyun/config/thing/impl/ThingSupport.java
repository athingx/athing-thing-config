package com.github.athingx.athing.aliyun.config.thing.impl;

import com.github.athingx.athing.aliyun.thing.ThingBoot;
import com.github.athingx.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.athingx.athing.aliyun.thing.runtime.access.ThingAccessImpl;
import com.github.athingx.athing.standard.api.ThingCom;
import com.github.athingx.athing.standard.thing.Thing;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import static java.lang.String.format;

public class ThingSupport {

    // 基础常量
    protected static final Properties properties = loadingProperties(new Properties());

    private static final ThingAccess THING_ACCESS = new ThingAccessImpl(
            $("athing.product.id"),
            $("athing.thing.id"),
            $("athing.thing.secret")
    );

    protected static Thing thing;

    @BeforeClass
    public static void initialization() throws Exception {
        thing = initPuppetThing();
    }

    @AfterClass
    public static void destroy() throws Exception {
        thing.destroy();
    }


    private static void reconnect(Thing thing) {
        if (!thing.isDestroyed()) {
            thing.getThingOp().connect()
                    .awaitUninterruptible()
                    .onFailure(connF -> reconnect(thing))
                    .onSuccess(connF -> connF.getSuccess().getDisconnectFuture().onDone(disconnectF -> reconnect(thing)));
        }
    }

    private static Thing initPuppetThing() throws Exception {
        final Thing thing = new ThingBoot(new URI($("athing.thing.server-url")), THING_ACCESS)
                .load((productId, thingId) -> new ThingCom[]{
                        new ConfigThingComImpl(new ConfigOption())
                })
                .boot();
        reconnect(thing);
        return thing;
    }

    /**
     * 初始化配置文件
     *
     * @param properties 配置信息
     * @return 配置信息
     */
    private static Properties loadingProperties(Properties properties) {

        // 读取配置文件
        final File file = new File(System.getProperties().getProperty("athing-qatest.properties.file"));

        // 检查文件是否存在
        if (!file.exists()) {
            throw new RuntimeException(format("properties file: %s not existed!", file.getAbsolutePath()));
        }

        // 检查文件是否可读
        if (!file.canRead()) {
            throw new RuntimeException(format("properties file: %s can not read!", file.getAbsolutePath()));
        }

        // 加载配置文件
        try (final InputStream is = new FileInputStream(file)) {
            properties.load(is);
            return properties;
        } catch (Exception cause) {
            throw new RuntimeException(format("properties file: %s load error!", file.getAbsoluteFile()), cause);
        }
    }

    private static String $(String name) {
        return properties.getProperty(name);
    }

}
