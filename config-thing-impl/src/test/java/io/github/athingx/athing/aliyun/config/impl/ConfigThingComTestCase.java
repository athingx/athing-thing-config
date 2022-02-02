package io.github.athingx.athing.aliyun.config.impl;

import io.github.athingx.athing.aliyun.config.Config;
import io.github.athingx.athing.aliyun.config.ConfigThingCom;
import io.github.athingx.athing.aliyun.config.Scope;
import io.github.oldmanpushcart.jpromisor.ListenableFuture;
import org.junit.Assert;
import org.junit.Test;

import static io.github.athingx.athing.aliyun.common.util.CommonUtils.isBlankString;

public class ConfigThingComTestCase extends ThingSupport {

    @Test
    public void test$thing$config$fetch$success() throws Exception {

        final ListenableFuture<Config> fetchF = thing.getUniqueThingCom(ConfigThingCom.class)
                .fetch(Scope.PRODUCT);

        fetchF.awaitUninterruptible();
        final Config config = fetchF.getSuccess();

        Assert.assertNotNull(config);
        Assert.assertFalse(isBlankString(config.getVersion()));
        Assert.assertEquals(Scope.PRODUCT, config.getScope());
        Assert.assertFalse(isBlankString(config.getContent().get()));

    }

}
