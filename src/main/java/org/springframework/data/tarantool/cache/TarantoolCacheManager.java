package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link org.springframework.cache.CacheManager} backed by a {@link TarantoolCache Tarantool} cache.
 *
 * @author Tatiana Blinova
 * @see TarantoolCacheConfiguration
 * @see TarantoolNativeCache
 */
public class TarantoolCacheManager extends AbstractCacheManager {

    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;
    private final TarantoolCacheConfiguration defaultCacheConfig;
    private final Map<String, TarantoolCacheConfiguration> initialCacheConfiguration;
    private final boolean allowInFlightCacheCreation;

    public TarantoolCacheManager(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                 TarantoolConverter tarantoolConverter,
                                 TarantoolCacheConfiguration defaultCacheConfig,
                                 boolean allowInFlightCacheCreation) {
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");
        Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null");
        Assert.notNull(defaultCacheConfig, "DefaultCacheConfiguration must not be null");

        this.tarantoolClient = tarantoolClient;
        this.tarantoolConverter = tarantoolConverter;
        this.defaultCacheConfig = defaultCacheConfig;
        this.initialCacheConfiguration = new LinkedHashMap<>();
        this.allowInFlightCacheCreation = allowInFlightCacheCreation;
    }

    public TarantoolCacheManager(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                 TarantoolConverter tarantoolConverter,
                                 TarantoolCacheConfiguration defaultCacheConfiguration) {
        this(tarantoolClient, tarantoolConverter, defaultCacheConfiguration, true);
    }

    public TarantoolCacheManager(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                 TarantoolConverter tarantoolConverter,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 String... initialCacheNames) {
        this(tarantoolClient, tarantoolConverter, defaultCacheConfiguration, true, initialCacheNames);
    }

    public TarantoolCacheManager(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                 TarantoolConverter tarantoolConverter,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 boolean allowInFlightCacheCreation,
                                 String... initialCacheNames) {
        this(tarantoolClient, tarantoolConverter, defaultCacheConfiguration, allowInFlightCacheCreation);

        for (String cacheName : initialCacheNames) {
            this.initialCacheConfiguration.put(cacheName, defaultCacheConfiguration);
        }
    }

    public TarantoolCacheManager(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                 TarantoolConverter tarantoolConverter,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 Map<String, TarantoolCacheConfiguration> initialCacheConfigurations) {
        this(tarantoolClient, tarantoolConverter, defaultCacheConfiguration, initialCacheConfigurations, true);
    }

    public TarantoolCacheManager(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                 TarantoolConverter tarantoolConverter,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 Map<String, TarantoolCacheConfiguration> initialCacheConfigurations,
                                 boolean allowInFlightCacheCreation) {
        this(tarantoolClient, tarantoolConverter, defaultCacheConfiguration, allowInFlightCacheCreation);

        Assert.notNull(initialCacheConfigurations, "InitialCacheConfigurations must not be null");

        this.initialCacheConfiguration.putAll(initialCacheConfigurations);
    }

    public static TarantoolCacheManager create(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                               TarantoolConverter tarantoolConverter) {
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");

        return new TarantoolCacheManager(tarantoolClient, tarantoolConverter, TarantoolCacheConfiguration.defaultCacheConfig());
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return initialCacheConfiguration.entrySet().stream()
                .map(entry -> createTarantoolCache(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    protected TarantoolCache getMissingCache(String name) {
        return allowInFlightCacheCreation ? createTarantoolCache(name, defaultCacheConfig) : null;
    }

    protected TarantoolCache createTarantoolCache(String name, @Nullable TarantoolCacheConfiguration cacheConfig) {
        return new TarantoolCache(name, cacheConfig != null ? cacheConfig : defaultCacheConfig, tarantoolClient, tarantoolConverter);
    }
}
