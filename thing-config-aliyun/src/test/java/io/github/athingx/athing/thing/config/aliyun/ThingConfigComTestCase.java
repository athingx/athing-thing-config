package io.github.athingx.athing.thing.config.aliyun;

import io.github.athingx.athing.thing.config.Config;
import io.github.athingx.athing.thing.config.Scope;
import io.github.athingx.athing.thing.config.ThingConfigCom;
import io.github.oldmanpushcart.jpromisor.ListenableFuture;
import org.junit.Assert;
import org.junit.Test;

import static io.github.athingx.athing.aliyun.common.util.CommonUtils.isBlankString;

public class ThingConfigComTestCase extends ThingSupport {

    @Test
    public void test$thing$config$fetch$success() throws Exception {

        final ListenableFuture<Config> fetchF = thingConfigCom.fetch(Scope.PRODUCT);

        final Config config = fetchF.get();

        Assert.assertNotNull(config);
        Assert.assertFalse(isBlankString(config.getVersion()));
        Assert.assertEquals(Scope.PRODUCT, config.getScope());
        Assert.assertFalse(isBlankString(config.getContent().get()));

    }

}
