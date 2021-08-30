package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.TarantoolTupleImpl;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Default implementation of {@link TarantoolNativeCache}, which provides low level access to Tarantool space operations used for
 * caching.
 *
 * @author Tatiana Blinova
 */
public class DefaultTarantoolNativeCache implements TarantoolNativeCache, TarantoolClientAware {
    private static final Logger log = LoggerFactory.getLogger(DefaultTarantoolNativeCache.class);

    private static final LocalDateTime MAX_TIME = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.MAX_VALUE), ZoneId.systemDefault());

    private final String spaceName;
    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;

    public DefaultTarantoolNativeCache(String cacheName,
                                       TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                       TarantoolConverter tarantoolConverter) {
        this(cacheName, null, tarantoolClient, tarantoolConverter);
    }

    public DefaultTarantoolNativeCache(String cacheName, @Nullable String cacheNamePrefix,
                                       TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                       TarantoolConverter tarantoolConverter) {
        Assert.notNull(cacheName, "CacheName must not be null");
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");
        Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null");

        this.spaceName = cacheSpaceName(cacheName, cacheNamePrefix);
        this.tarantoolClient = tarantoolClient;
        this.tarantoolConverter = tarantoolConverter;

        if (spaceMetadata(spaceName).isPresent()) {
            log.debug("Space {} for caching was created earlier", spaceName);
        } else if (!isProxyClient()) {
            log.debug("Create space {} for caching", spaceName);
            createCacheSpace();
        } else {
            throw new UnsupportedOperationException("Auto space creation for caching not supported with used client. Please create space manually");
        }
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
        return unwrap(execute(spaceOps -> spaceOps.select(primaryIndexQuery(key))))
                .stream()
                .findFirst()
                .map(tuple -> {
                    TarantoolCacheEntry cacheEntry = tupleToCacheEntry(tuple);
                    if (cacheEntry.getExpiryTime().isAfter(LocalDateTime.now())) {
                        return cacheEntry.getValue();
                    } else {
                        remove(cacheEntry.getKey());
                        return null;
                    }
                })
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
        byte[] existedValue = get(key);
        if (existedValue == null) {
            put(key, value, ttl);
        }
        return existedValue;
    }

    @Override
    public void remove(byte[] key) {
        unwrap(execute(spaceOps -> spaceOps.delete(primaryIndexQuery(key))));
    }

    @Override
    public void remove() {
        if (isProxyClient()) {
            unwrap(tarantoolClient.call("crud.truncate", List.of(spaceName)));
        } else {
            unwrap(tarantoolClient.call(String.format("box.space.%s:truncate", spaceName)));
        }
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

    private String cacheSpaceName(String cacheName, @Nullable String cacheNamePrefix) {
        String preparedCacheName = cacheName.replaceAll("[^a-zA-Z0-9]", "_");
        if (cacheNamePrefix != null) {
            return String.format("%s_%s", cacheNamePrefix, preparedCacheName);
        }
        return preparedCacheName;
    }

    private void createCacheSpace() {
        unwrap(tarantoolClient.eval(String.format("box.schema.space.create('%s')", spaceName)));

        unwrap(
                tarantoolClient.call(String.format("box.space.%s:format", spaceName), cacheSpaceFormatParams())
                        .thenCompose(r -> tarantoolClient.call(String.format("box.space.%s:create_index", spaceName), cacheSpaceIndexParams()))
                        .thenCompose(r -> tarantoolClient.metadata().refresh())
                        .whenComplete((r, e) -> {
                            if (e != null) {
                                log.error(String.format("Error while format space %s and create primary index, drop space", spaceName), e);
                                unwrap(tarantoolClient.eval(String.format("box.space.%s:drop", spaceName)));
                            }
                        })
        );
    }

    private Object cacheSpaceFormatParams() {
        return List.of(
                Map.of("name", "key", "type", "scalar"),
                Map.of("name", "value", "type", "any"),
                Map.of("name", "expiry_time", "type", "unsigned")
        );
    }

    private List<Object> cacheSpaceIndexParams() {
        return List.of("primary", Map.of("parts", List.of("key")));
    }
}
