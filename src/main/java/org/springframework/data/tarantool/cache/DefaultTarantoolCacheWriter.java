package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.metadata.TarantoolSpaceMetadata;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.core.tuple.TarantoolTupleImpl;
import io.tarantool.driver.mappers.MessagePackMapper;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation of {@link TarantoolCacheWriter}, which provides low level access to Tarantool space operations used for
 * caching.
 *
 * @author Alexander Rublev
 */
public class DefaultTarantoolCacheWriter implements TarantoolCacheWriter, TarantoolClientAware {
    private static final Logger log = LoggerFactory.getLogger(DefaultTarantoolCacheWriter.class);
    private static final LocalDateTime MAX_TIME = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.MAX_VALUE), ZoneId.systemDefault());

    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;
    private final CacheStatisticsCollector statistics;
    private final SpaceNameProvider spaceNames;
    private final Consumer<String> spaceCreator;

    /**
     * @param tarantoolClient    must not be {@literal null}.
     * @param tarantoolConverter must not be {@literal null}.
     */
    public DefaultTarantoolCacheWriter(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                       TarantoolConverter tarantoolConverter) {
        this(tarantoolClient, tarantoolConverter, CacheStatisticsCollector.none(), SpaceNameProvider.create());
    }

    /**
     * @param tarantoolClient    must not be {@literal null}.
     * @param tarantoolConverter must not be {@literal null}.
     * @param statistics         must not be {@literal null}.
     * @param spaceNames         must not be {@literal null}.
     */
    public DefaultTarantoolCacheWriter(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                       TarantoolConverter tarantoolConverter,
                                       CacheStatisticsCollector statistics,
                                       SpaceNameProvider spaceNames) {
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null!");
        Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null!");
        Assert.notNull(statistics, "CacheStatisticsCollector must not be null!");
        Assert.notNull(spaceNames, "SpaceNameProvider must not be null!");

        this.tarantoolClient = tarantoolClient;
        this.tarantoolConverter = tarantoolConverter;
        this.statistics = statistics;
        this.spaceNames = spaceNames;
        this.spaceCreator = spaceName -> {
            if (spaceMetadata(spaceName).isPresent()) {
                log.debug("Space {} for caching was created earlier", spaceName);
            } else {
                log.debug("Create space {} for caching", spaceName);
                unwrap(this.tarantoolClient.eval(String.format("box.schema.space.create('%s')", spaceName)));

                Object cacheSpaceFormatParams = List.of(
                        Map.of("name", "key", "type", "varbinary"),
                        Map.of("name", "value", "type", "varbinary"),
                        Map.of("name", "expiry_time", "type", "unsigned")
                );

                List<Object> cacheSpacePrimaryIndexParams = List.of("primary", Map.of("parts", List.of("key")));

                List<Object> cacheSpaceExpiryTimeIndexParams = List.of("expiry_time", Map.of("parts", List.of("expiry_time"), "unique", false));

                unwrap(this.tarantoolClient.call(String.format("box.space.%s:format", spaceName), cacheSpaceFormatParams)
                        .thenCompose(r -> tarantoolClient.call(String.format("box.space.%s:create_index", spaceName), cacheSpacePrimaryIndexParams))
                        .thenCompose(r -> tarantoolClient.call(String.format("box.space.%s:create_index", spaceName), cacheSpaceExpiryTimeIndexParams))
                        .thenCompose(r -> tarantoolClient.metadata().refresh())
                        .whenComplete((r, e) -> {
                            if (e != null) {
                                log.error(String.format("Error while format space %s and create primary index, drop space", spaceName), e);
                                unwrap(tarantoolClient.eval(String.format("box.space.%s:drop", spaceName)));
                            }
                        })
                );
            }
        };
    }

    @Nullable
    @Override
    public byte[] get(String name, byte[] key) {
        String spaceName = getSpaceName(name);

        byte[] result = unwrap(execute(spaceName, spaceOps -> spaceOps.select(primaryIndexQuery(key))))
                .stream()
                .findFirst()
                .map(tuple -> {
                    TarantoolCacheEntry cacheEntry = tupleToCacheEntry(tuple);
                    if (cacheEntry.getExpiryTime().isAfter(LocalDateTime.now())) {
                        return cacheEntry.getValue();
                    } else {
                        remove(name, cacheEntry.getKey());
                        return null;
                    }
                })
                .orElse(null);

        statistics.incGets(name);

        if (result != null) {
            statistics.incHits(name);
        } else {
            statistics.incMisses(name);
        }

        return result;
    }

    @Override
    public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
        String spaceName = getSpaceName(name);

        LocalDateTime expiryTime = ttl != null && !ttl.isZero() && !ttl.isNegative() ? LocalDateTime.now().plusSeconds(ttl.getSeconds()) : MAX_TIME;
        TarantoolCacheEntry cacheEntry = TarantoolCacheEntry.of(key, value, expiryTime);
        unwrap(execute(spaceName, spaceOps -> spaceOps.replace(cacheEntryToTuple(cacheEntry, tarantoolClient.getConfig().getMessagePackMapper(), requiredSpaceMetadata(spaceName)))));

        statistics.incPuts(name);
    }

    @Nullable
    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
        byte[] existedValue = get(name, key);
        if (existedValue == null) {
            put(name, key, value, ttl);
        }
        return existedValue;
    }

    @Override
    public void remove(String name, byte[] key) {
        unwrap(execute(getSpaceName(name), spaceOps -> spaceOps.delete(primaryIndexQuery(key))));
        statistics.incDeletes(name);
    }

    @Override
    public void clear(String name) {
        unwrap(tarantoolClient.call(String.format("box.space.%s:truncate", getSpaceName(name))));
    }

    @Override
    public void clearStatistics(String name) {
        statistics.reset(name);
    }

    @Override
    public CacheStatistics getCacheStatistics(String name) {
        return statistics.getCacheStatistics(name);
    }

    @Override
    public TarantoolCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        return new DefaultTarantoolCacheWriter(tarantoolClient, tarantoolConverter, cacheStatisticsCollector, spaceNames);
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

    private String getSpaceName(String name) {
        return spaceNames.get(name, spaceCreator);
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

    private <R> CompletableFuture<R> execute(String spaceName, Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
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

    private static class SpaceNameProvider {
        private final Map<String, String> names = new ConcurrentHashMap<>();

        private SpaceNameProvider() {
        }

        String get(String cacheName, Consumer<String> spaceCreator) {
            return names.computeIfAbsent(cacheName, n -> {
                spaceCreator.accept(n);
                return n;
            });
        }

        static SpaceNameProvider create() {
            return new SpaceNameProvider();
        }
    }
}
