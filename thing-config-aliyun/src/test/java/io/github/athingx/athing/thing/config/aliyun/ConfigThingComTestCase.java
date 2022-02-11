package io.github.athingx.athing.thing.config.aliyun;

import io.github.athingx.athing.standard.thing.boot.ThingBoot;
import io.github.athingx.athing.thing.config.Config;
import io.github.athingx.athing.thing.config.ConfigThingCom;
import io.github.athingx.athing.thing.config.Scope;
import io.github.oldmanpushcart.jpromisor.ListenableFuture;
import org.junit.Assert;
import org.junit.Test;

import static io.github.athingx.athing.aliyun.common.util.CommonUtils.isBlankString;

public class ConfigThingComTestCase extends ThingSupport {

    @Test
    public void test$thing$config$fetch$success() throws Exception {

        final ListenableFuture<Config> fetchF = thing.getUniqueThingCom(ConfigThingCom.class)
                .fetch(Scope.PRODUCT);

        final Config config = fetchF.get();

        Assert.assertNotNull(config);
        Assert.assertFalse(isBlankString(config.getVersion()));
        Assert.assertEquals(Scope.PRODUCT, config.getScope());
        Assert.assertFalse(isBlankString(config.getContent().get()));

    }

    @Test
    public void test$thing$modular$boot() {
        final ThingBoot boot = new ConfigThingBoot();
        Assert.assertEquals("athing", boot.getProperties().getProperty("manufacturer"));
        Assert.assertEquals("athing-thing-config", boot.getProperties().getProperty("model"));
        Assert.assertEquals("aliyun", boot.getProperties().getProperty("framework"));
        Assert.assertEquals("oldmanpushcart@gmail.com", boot.getProperties().getProperty("author"));
        Assert.assertEquals("${project.version}", boot.getProperties().getProperty("version"));
    }

}
