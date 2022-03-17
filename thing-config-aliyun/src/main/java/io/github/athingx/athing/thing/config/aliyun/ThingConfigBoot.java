package io.github.athingx.athing.thing.config.aliyun;

import io.github.athingx.athing.standard.component.ThingCom;
import io.github.athingx.athing.standard.thing.boot.ThingBoot;
import io.github.athingx.athing.standard.thing.boot.ThingBootArgument;

import java.io.InputStream;
import java.util.Properties;

import static io.github.athingx.athing.standard.thing.boot.ThingBootArgument.Converter.cLong;

/**
 * 设备配置组件引导程序
 * <p>
 * {@code timeout_ms=180000&connect_timeout_ms=60000}
 * </p>
 */
public class ThingConfigBoot implements ThingBoot {

    @Override
    public ThingCom[] boot(String productId, String thingId, ThingBootArgument argument) {
        return new ThingCom[]{
                new ThingConfigComImpl(toOption(argument))
        };
    }

    private ConfigOption toOption(ThingBootArgument argument) {
        final ConfigOption option = new ConfigOption();
        if (null != argument) {
            argument.optionArgument("timeout_ms", cLong, option::setTimeoutMs);
            argument.optionArgument("connect_timeout_ms", cLong, option::setConnectTimeoutMs);
        }
        return option;
    }

    @Override
    public Properties getProperties() {
        return new Properties(){{
            put(PROP_GROUP, "io.github.athingx.athing.thing.config");
            put(PROP_ARTIFACT, "thing-config-aliyun");
        }};
    }

}
