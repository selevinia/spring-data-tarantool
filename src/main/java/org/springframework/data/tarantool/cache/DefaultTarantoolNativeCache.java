package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Duration;

public class DefaultTarantoolNativeCache implements TarantoolNativeCache {

    private final String cacheName;
    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;

    public DefaultTarantoolNativeCache(String cacheName, TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        Assert.notNull(cacheName, "Cache name must not be null");
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");

        this.cacheName = cacheName;
        this.tarantoolClient = tarantoolClient;
    }

    @Override
    public void put(Object key, Object value, @Nullable Duration ttl) {

    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public Object putIfAbsent(Object key, Object value, @Nullable Duration ttl) {
        return null;
    }

    @Override
    public void remove(Object key) {

    }

    @Override
    public void remove() {

    }
}
