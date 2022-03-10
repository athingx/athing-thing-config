module io.github.athingx.athing.thing.config.aliyun {

    requires io.github.athingx.athing.thing.config;
    requires io.github.athingx.athing.aliyun.thing.runtime;
    requires com.google.gson;
    requires org.slf4j;

    exports io.github.athingx.athing.thing.config.aliyun;

    opens io.github.athingx.athing.thing.config.aliyun to com.google.gson;
    opens io.github.athingx.athing.thing.config.aliyun.domain to com.google.gson;
    provides io.github.athingx.athing.standard.thing.boot.ThingBoot
            with io.github.athingx.athing.thing.config.aliyun.ThingConfigBoot;

}