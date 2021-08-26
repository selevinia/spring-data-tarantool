package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.TarantoolTupleImpl;
import io.tarantool.driver.api.tuple.operations.TupleOperations;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import io.tarantool.driver.protocol.TarantoolIndexQuery;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.tarantool.TarantoolCacheAccessException;
import org.springframework.data.tarantool.core.TarantoolClientAware;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

// todo - set lock for write operations?
public class DefaultTarantoolNativeCache implements TarantoolNativeCache, TarantoolClientAware {

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
    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> getClient() {
        return tarantoolClient;
    }

    @Override
    public DataAccessException dataAccessException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return new TarantoolCacheAccessException(throwable.getMessage(), throwable);
        }
        return new DataRetrievalFailureException(throwable.getMessage(), throwable);
    }

    @Override
    public byte[] get(byte[] key) {
        // todo - add deletion of expired element
        return unwrap(execute(spaceOps -> spaceOps.select(primaryIndexQuery(key))))
                .stream()
                .findFirst()
                .map(this::tupleToCacheEntry)
                .filter(cacheEntry -> cacheEntry.getExpiryTime().isAfter(LocalDateTime.now()))
                .map(TarantoolCacheEntry::getValue)
                .orElse(null);
    }

    @Override
    public void put(byte[] key, byte[] value, @Nullable Duration ttl) {
        LocalDateTime expiryTime = ttl != null ? LocalDateTime.now().plusSeconds(ttl.getSeconds()) : LocalDateTime.MAX;
        TarantoolCacheEntry cacheEntry = TarantoolCacheEntry.of(key, value, expiryTime);
        unwrap(execute(spaceOps -> spaceOps.replace(cacheEntryToTuple(cacheEntry, tarantoolClient.getConfig().getMessagePackMapper(), requiredSpaceMetadata(spaceName)))));
    }

    @Override
    public byte[] putIfAbsent(byte[] key, byte[] value, @Nullable Duration ttl) {
        LocalDateTime expiryTime = ttl != null ? LocalDateTime.now().plusSeconds(ttl.getSeconds()) : LocalDateTime.MAX;
        TarantoolCacheEntry cacheEntry = TarantoolCacheEntry.of(key, value, expiryTime);

        TarantoolTuple tuple = cacheEntryToTuple(cacheEntry, tarantoolClient.getConfig().getMessagePackMapper(), requiredSpaceMetadata(spaceName));
        TupleOperations updateFakeField = TupleOperations.set(4, null);
        unwrap(execute(spaceOps -> spaceOps.upsert(primaryIndexQuery(key), tuple, updateFakeField)));
        return value;
    }

    @Override
    public void remove(byte[] key) {
        unwrap(execute(spaceOps -> spaceOps.delete(primaryIndexQuery(key))));
    }

    @Override
    public void remove() {
        unwrap(execute(spaceOps -> spaceOps.delete(Conditions.any())));
    }

    private TarantoolTuple cacheEntryToTuple(TarantoolCacheEntry cacheEntry, MessagePackMapper mapper, TarantoolSpaceMetadata spaceMetadata) {
        TarantoolTuple tuple = new TarantoolTupleImpl(mapper, spaceMetadata);
        tarantoolConverter.write(cacheEntry, tuple);
        return tuple;
    }

    private TarantoolCacheEntry tupleToCacheEntry(Object tuple) {
        return tarantoolConverter.read(TarantoolCacheEntry.class, tuple);
    }

    private Conditions primaryIndexQuery(byte[] key) {
        return Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(tarantoolConverter.convertToWritableType(key)));
    }

    private <R> CompletableFuture<R> execute(Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        try {
            return operation.apply(spaceOperations(spaceName));
        } catch (Throwable throwable) {
            return CompletableFuture.failedFuture(throwable);
        }
    }

    private <R> R unwrap(Future<R> f) {
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw dataAccessException(e.getCause());
        } catch (InterruptedException e) {
            throw new TarantoolCacheAccessException(e.getMessage(), e);
        }
    }
}
