package io.github.athingx.athing.thing.config.aliyun;

import io.github.athingx.athing.standard.component.ThingCom;
import io.github.athingx.athing.standard.thing.boot.ThingBoot;
import io.github.athingx.athing.standard.thing.boot.ThingBootArgument;
import org.kohsuke.MetaInfServices;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.util.Properties;

import static io.github.athingx.athing.standard.thing.boot.ThingBootArgument.Converter.cLong;

/**
 * 设备配置组件引导程序
 * <p>
 * {@code -Dathing.aliyun.config.boot="timeout=180000&connect-timeout=60000"}
 * </p>
 */
@MetaInfServices
public class ConfigThingBoot implements ThingBoot {

    @Override
    public ThingCom[] boot(String productId, String thingId, ThingBootArgument argument) {
        return new ThingCom[]{
                new ConfigThingComImpl(merge(
                        new ConfigOption(),
                        argument,
                        ThingBootArgument.parse(System.getProperty("athing.aliyun.config.boot"))
                ))
        };
    }

    private ConfigOption merge(ConfigOption option, ThingBootArgument... arguments) {
        if (null == arguments) {
            return option;
        }
        for (final ThingBootArgument argument : arguments) {
            if (argument.hasArguments("timeout")) {
                option.setTimeoutMs(argument.getArgument("timeout", cLong));
            }
            if (argument.hasArguments("connect-timeout")) {
                option.setConnectTimeoutMs(argument.getArgument("connect-timeout", cLong));
            }
        }
        return option;
    }

    @Override
    public Properties getProperties() {
        final Properties prop = ThingBoot.super.getProperties();
        try (final InputStream in = Object.class.getResourceAsStream("config-thing-boot.properties")) {
            if (null != in) {
                prop.load(in);
            }
        } catch (Exception cause) {
            // ignore...
        }
        return prop;
    }
}
