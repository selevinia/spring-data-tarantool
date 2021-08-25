package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
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
 * @see TarantoolCacheWriter
 */
public class TarantoolCacheManager extends AbstractCacheManager {

    private final TarantoolCacheWriter cacheWriter;
    private final TarantoolCacheConfiguration defaultCacheConfig;
    private final Map<String, TarantoolCacheConfiguration> initialCacheConfiguration;
    private final boolean allowInFlightCacheCreation;

    public TarantoolCacheManager(TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration defaultCacheConfig,
                                 boolean allowInFlightCacheCreation) {
        Assert.notNull(cacheWriter, "CacheWriter must not be null");
        Assert.notNull(defaultCacheConfig, "DefaultCacheConfiguration must not be null");

        this.cacheWriter = cacheWriter;
        this.defaultCacheConfig = defaultCacheConfig;
        this.initialCacheConfiguration = new LinkedHashMap<>();
        this.allowInFlightCacheCreation = allowInFlightCacheCreation;
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration defaultCacheConfiguration) {
        this(cacheWriter, defaultCacheConfiguration, true);
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration defaultCacheConfiguration, String... initialCacheNames) {
        this(cacheWriter, defaultCacheConfiguration, true, initialCacheNames);
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration defaultCacheConfiguration,
                                 boolean allowInFlightCacheCreation, String... initialCacheNames) {
        this(cacheWriter, defaultCacheConfiguration, allowInFlightCacheCreation);

        for (String cacheName : initialCacheNames) {
            this.initialCacheConfiguration.put(cacheName, defaultCacheConfiguration);
        }
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration defaultCacheConfiguration,
                                 Map<String, TarantoolCacheConfiguration> initialCacheConfigurations) {
        this(cacheWriter, defaultCacheConfiguration, initialCacheConfigurations, true);
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration defaultCacheConfiguration,
                                 Map<String, TarantoolCacheConfiguration> initialCacheConfigurations, boolean allowInFlightCacheCreation) {
        this(cacheWriter, defaultCacheConfiguration, allowInFlightCacheCreation);

        Assert.notNull(initialCacheConfigurations, "InitialCacheConfigurations must not be null");

        this.initialCacheConfiguration.putAll(initialCacheConfigurations);
    }

    public static TarantoolCacheManager create(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");

        return new TarantoolCacheManager(new DefaultTarantoolCacheWriter(tarantoolClient), TarantoolCacheConfiguration.defaultCacheConfig());
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
        return new TarantoolCache(name, cacheWriter, cacheConfig != null ? cacheConfig : defaultCacheConfig);
    }
}
