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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.tarantool.TarantoolCacheAccessException;
import org.springframework.data.tarantool.core.TarantoolClientAware;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class DefaultTarantoolNativeCache implements TarantoolNativeCache, TarantoolClientAware {
    private static final Logger log = LoggerFactory.getLogger(DefaultTarantoolNativeCache.class);

    private static final LocalDateTime MAX_TIME = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.MAX_VALUE), ZoneId.systemDefault());

    private final String spaceName;
    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;

    public DefaultTarantoolNativeCache(String cacheName,
                                       TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                       TarantoolConverter tarantoolConverter) {
        Assert.notNull(cacheName, "Cache name must not be null");
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");

        this.spaceName = cacheName.replaceAll("[^a-zA-Z0-9]", "_"); // todo - generate spaceName and create space
        this.tarantoolClient = tarantoolClient;
        this.tarantoolConverter = tarantoolConverter;

        createSpaceIfNeeded();
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
        LocalDateTime expiryTime = ttl != null ? LocalDateTime.now().plusSeconds(ttl.getSeconds()) : MAX_TIME;
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

    private void createSpaceIfNeeded() {
        Optional<TarantoolSpaceMetadata> spaceMetadata = spaceMetadata(spaceName);
        if (spaceMetadata.isEmpty()) {
            unwrap(tarantoolClient.eval(String.format("box.schema.space.create('%s')", spaceName)));

            unwrap(
                    tarantoolClient.call(String.format("box.space.%s:format", spaceName), spaceFormatParams())
                            .thenCompose(r -> tarantoolClient.call(String.format("box.space.%s:create_index", spaceName), spaceIndexParams()))
                            .thenCompose(r -> tarantoolClient.metadata().refresh())
                            .whenComplete((r, e) -> {
                                if (e != null) {
                                    log.error(String.format("Error while format space %s", spaceName), e);
                                    dropSpace();
                                }
                            })
            );
        }
    }

    private List<?> dropSpace() {
        return unwrap(tarantoolClient.eval(String.format("box.space.%s:drop", spaceName)));
    }

    private Object spaceFormatParams() {
        return List.of(
                Map.of("name", "key", "type", "scalar"),
                Map.of("name", "value", "type", "any"),
                Map.of("name", "expiry_time", "type", "unsigned")
        );
    }

    private List<Object> spaceIndexParams() {
        return List.of("primary", Map.of("parts", List.of("key")));
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
