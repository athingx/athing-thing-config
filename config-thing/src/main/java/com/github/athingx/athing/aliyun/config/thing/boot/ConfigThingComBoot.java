package com.github.athingx.athing.aliyun.config.thing.boot;

import com.github.athingx.athing.aliyun.config.thing.component.impl.ConfigOption;
import com.github.athingx.athing.aliyun.config.thing.component.impl.ConfigThingComImpl;
import com.github.athingx.athing.standard.api.ThingCom;
import com.github.athingx.athing.standard.thing.boot.BootArguments;
import com.github.athingx.athing.standard.thing.boot.ThingComBoot;

import static com.github.athingx.athing.standard.thing.boot.BootArguments.Converter.cLong;

/**
 * 设备配置组件引导程序
 */
public class ConfigThingComBoot implements ThingComBoot {

    @Override
    public ThingCom[] boot(String productId, String thingId, BootArguments arguments) {
        return new ThingCom[]{
                new ConfigThingComImpl(toOption(arguments))
        };
    }

    private ConfigOption toOption(BootArguments arguments) {
        final ConfigOption option = new ConfigOption();
        if (arguments.hasArguments("timeout")) {
            option.setTimeoutMs(arguments.getArgument("timeout", cLong));
        }
        if (arguments.hasArguments("connect-timeout")) {
            option.setConnectTimeoutMs(arguments.getArgument("connect-timeout", cLong));
        }
        return option;
    }

}
