package com.github.athingx.athing.aliyun.config.boot;

import com.github.athingx.athing.aliyun.config.component.ConfigOption;
import com.github.athingx.athing.aliyun.config.component.DefaultConfigThingCom;
import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.thing.boot.BootArguments;
import com.github.athingx.athing.standard.thing.boot.ThingComBoot;
import org.kohsuke.MetaInfServices;

import static com.github.athingx.athing.standard.thing.boot.BootArguments.Converter.cLong;

/**
 * 配置组件启动器
 */
@MetaInfServices
public class BootImpl implements ThingComBoot {

    private static final String OPT_CONNECT_TIMEOUT = "connect-timeout";
    private static final String OPT_TIMEOUT = "timeout";

    @Override
    public ThingCom bootUp(String productId, String thingId, BootArguments arguments) {
        final ConfigOption option = new ConfigOption();

        // 下载配置连接超时时间，默认：1分钟
        option.setConnectTimeoutMs(arguments.getArgument(OPT_CONNECT_TIMEOUT, cLong, 1000L * 60));

        // 下载配置超时时间，默认：3分钟
        option.setTimeoutMs(arguments.getArgument(OPT_TIMEOUT, cLong, 3L * 1000 * 60));

        // 生成配置组件
        return new DefaultConfigThingCom(option);
    }

}
