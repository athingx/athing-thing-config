package io.github.athingx.athing.thing.config.aliyun;

import io.github.athingx.athing.standard.component.ThingCom;
import io.github.athingx.athing.standard.thing.boot.ThingBoot;
import io.github.athingx.athing.standard.thing.boot.ThingBootArgument;
import org.kohsuke.MetaInfServices;

import java.io.InputStream;
import java.util.Properties;

import static io.github.athingx.athing.standard.thing.boot.ThingBootArgument.Converter.cLong;

/**
 * 设备配置组件引导程序
 * <p>
 * {@code timeout_ms=180000&connect_timeout_ms=60000}
 * </p>
 */
@MetaInfServices
public class ThingConfigBoot implements ThingBoot {

    @Override
    public ThingCom[] boot(String productId, String thingId, ThingBootArgument argument) {
        return new ThingCom[]{
                new ThingConfigComImpl(merge(
                        new ConfigOption(),
                        argument
                ))
        };
    }

    private ConfigOption merge(ConfigOption option, ThingBootArgument... arguments) {
        if (null == arguments) {
            return option;
        }
        for (final ThingBootArgument argument : arguments) {
            if (argument.hasArguments("timeout_ms")) {
                option.setTimeoutMs(argument.getArgument("timeout_ms", cLong));
            }
            if (argument.hasArguments("connect_timeout_ms")) {
                option.setConnectTimeoutMs(argument.getArgument("connect_timeout_ms", cLong));
            }
        }
        return option;
    }

    @Override
    public Properties getProperties() {
        final Properties prop = ThingBoot.super.getProperties();
        try (final InputStream in = ThingConfigBoot.class.getResourceAsStream("/io/github/athingx/athing/thing/config/aliyun/thing-boot.properties")) {
            if (null != in) {
                prop.load(in);
            }
        } catch (Exception cause) {
            // ignore...
        }
        return prop;
    }
}
