package com.github.athingx.athing.aliyun.config.component;

import com.github.athingx.athing.aliyun.config.api.Committer;
import com.github.athingx.athing.standard.thing.op.executor.ThingPromise;

class CommitterImpl implements Committer {

    private final ThingPromise<Void> promise;

    public CommitterImpl(ThingPromise<Void> promise) {
        this.promise = promise;
    }

    @Override
    public boolean commit() {
        return promise.trySuccess();
    }

    @Override
    public boolean rollback(String reason) {
        return promise.tryException(new RuntimeException(reason));
    }

    @Override
    public boolean rollback(Throwable cause) {
        return promise.tryException(cause);
    }

}
