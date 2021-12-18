package com.github.athingx.athing.aliyun.config.thing.boot;

import com.github.athingx.athing.aliyun.config.thing.impl.ConfigOption;
import com.github.athingx.athing.aliyun.config.thing.impl.ConfigThingComImpl;
import com.github.athingx.athing.standard.api.ThingCom;
import com.github.athingx.athing.standard.thing.boot.BootArgument;
import com.github.athingx.athing.standard.thing.boot.ThingComBoot;
import org.kohsuke.MetaInfServices;

import java.io.InputStream;
import java.util.Properties;

import static com.github.athingx.athing.standard.thing.boot.BootArgument.Converter.cLong;

/**
 * 设备配置组件引导程序
 * <p>
 * {@code -Dathing.aliyun.config.boot="timeout=180000&connect-timeout=60000"}
 * </p>
 */
@MetaInfServices
public class ConfigThingComBoot implements ThingComBoot {

    @Override
    public ThingCom[] boot(String productId, String thingId, BootArgument argument) {
        return new ThingCom[]{
                new ConfigThingComImpl(merge(
                        new ConfigOption(),
                        argument,
                        BootArgument.parse(System.getProperty("athing.aliyun.config.boot"))
                ))
        };
    }

    private ConfigOption merge(ConfigOption option, BootArgument... arguments) {
        if (null == arguments) {
            return option;
        }
        for (final BootArgument argument : arguments) {
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
        final Properties prop = ThingComBoot.super.getProperties();
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
