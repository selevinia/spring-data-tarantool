package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;

public class DefaultTarantoolCacheWriter implements TarantoolCacheWriter {

    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;

    public DefaultTarantoolCacheWriter(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        this.tarantoolClient = tarantoolClient;
    }

    @Override
    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> getTarantoolClient() {
        return tarantoolClient;
    }
}
