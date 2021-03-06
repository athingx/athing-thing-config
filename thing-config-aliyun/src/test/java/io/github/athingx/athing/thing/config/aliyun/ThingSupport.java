package io.github.athingx.athing.thing.config.aliyun;

import io.github.athingx.athing.aliyun.thing.ThingBuilder;
import io.github.athingx.athing.aliyun.thing.runtime.access.ThingAccess;
import io.github.athingx.athing.standard.thing.Thing;
import io.github.athingx.athing.thing.config.ThingConfigCom;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

public class ThingSupport {

    // 基础常量
    protected static final Properties properties = loadingProperties(new Properties());
    protected static final String PRODUCT_ID = $("athing.product.id");
    protected static final String THING_ID = $("athing.thing.id");

    private static final ThingAccess THING_ACCESS = new ThingAccess(
            $("athing.product.id"),
            $("athing.thing.id"),
            $("athing.thing.secret")
    );

    protected static Thing thing;
    protected static ThingConfigCom thingConfigCom;

    @BeforeClass
    public static void initialization() throws Exception {
        thing = initPuppetThing();
    }

    @AfterClass
    public static void destroy() {
        thing.destroy();
    }


    private static void reconnect(Thing thing) {
        if (!thing.isDestroyed()) {
            thing.connect()
                    .awaitUninterruptible()
                    .onFailure(e -> reconnect(thing))
                    .onSuccess(v -> v.getDisconnectFuture().onDone(disconnectF -> reconnect(thing)));
        }
    }

    private static Thing initPuppetThing() throws Exception {
        final Thing thing = new ThingBuilder(new URI($("athing.thing.server-url")), THING_ACCESS)
                .executor(Executors.newFixedThreadPool(20))
                .build();
        reconnect(thing);
        thing.load(thingConfigCom = new DefaultThingConfigCom(thing, new ConfigOption()));
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
