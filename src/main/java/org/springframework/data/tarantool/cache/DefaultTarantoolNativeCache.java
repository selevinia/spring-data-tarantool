package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.protocol.TarantoolIndexQuery;
import org.springframework.data.tarantool.TarantoolCacheAccessException;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class DefaultTarantoolNativeCache implements TarantoolNativeCache {

    private final String spaceName;
    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;

    public DefaultTarantoolNativeCache(String cacheName,
                                       TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                       TarantoolConverter tarantoolConverter) {
        Assert.notNull(cacheName, "Cache name must not be null");
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");

        this.spaceName = cacheName; // todo - generate spaceName and create space
        this.tarantoolClient = tarantoolClient;
        this.tarantoolConverter = tarantoolConverter;
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
        Conditions query = Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(tarantoolConverter.convertToWritableType(key)));
        unwrap(execute(spaceOps -> spaceOps.delete(query)));
    }

    @Override
    public void remove() {
        unwrap(execute(spaceOps -> spaceOps.delete(Conditions.any())));
    }

    private <R> CompletableFuture<R> execute(Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        try {
            return operation.apply(tarantoolClient.space(spaceName));
        } catch (Throwable throwable) {
            return CompletableFuture.failedFuture(throwable);
        }
    }

    private <R> R unwrap(Future<R> f) {
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw new TarantoolCacheAccessException(e.getCause().getMessage(), e.getCause());
        } catch (InterruptedException e) {
            throw new TarantoolCacheAccessException(e.getMessage(), e);
        }
    }
}
