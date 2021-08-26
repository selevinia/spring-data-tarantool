package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.lang.Nullable;

import java.time.Duration;

public class DefaultTarantoolCacheWriter implements TarantoolCacheWriter {

    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;

    public DefaultTarantoolCacheWriter(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        this.tarantoolClient = tarantoolClient;
    }

    @Override
    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> getTarantoolClient() {
        return tarantoolClient;
    }

    @Override
    public void put(String name, Object key, Object value, @Nullable Duration ttl) {

    }

    @Override
    public Object get(String name, Object key) {
        return null;
    }

    @Override
    public Object putIfAbsent(String name, Object key, Object value, @Nullable Duration ttl) {
        return null;
    }

    @Override
    public void remove(String name, Object key) {

    }

    @Override
    public void remove(String name) {

    }
}
