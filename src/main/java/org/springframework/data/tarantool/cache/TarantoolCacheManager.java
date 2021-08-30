package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
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

    public static TarantoolCacheManagerBuilder builder() {
        return new TarantoolCacheManagerBuilder();
    }

    public static TarantoolCacheManagerBuilder builder(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                                       TarantoolConverter tarantoolConverter) {
        return new TarantoolCacheManagerBuilder(tarantoolClient, tarantoolConverter);
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

    public static class TarantoolCacheManagerBuilder {

        private TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
        private TarantoolConverter tarantoolConverter;
        private TarantoolCacheConfiguration defaultCacheConfiguration = TarantoolCacheConfiguration.defaultCacheConfig();
        private final Map<String, TarantoolCacheConfiguration> initialCaches = new LinkedHashMap<>();
        boolean allowInFlightCacheCreation = true;

        private TarantoolCacheManagerBuilder() {
        }

        private TarantoolCacheManagerBuilder(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient, TarantoolConverter tarantoolConverter) {
            Assert.notNull(tarantoolClient, "TarantoolClient must not be null");
            Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null");

            this.tarantoolClient = tarantoolClient;
            this.tarantoolConverter = tarantoolConverter;
        }

        public TarantoolCacheManagerBuilder cacheDefaults(TarantoolCacheConfiguration defaultCacheConfiguration) {
            Assert.notNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null");

            this.defaultCacheConfiguration = defaultCacheConfiguration;

            return this;
        }

        public TarantoolCacheManagerBuilder tarantoolClient(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
            Assert.notNull(tarantoolClient, "TarantoolClient must not be null");

            this.tarantoolClient = tarantoolClient;

            return this;
        }

        public TarantoolCacheManagerBuilder tarantoolConverter(TarantoolConverter tarantoolConverter) {
            Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null");

            this.tarantoolConverter = tarantoolConverter;

            return this;
        }

        public TarantoolCacheManagerBuilder initialCacheNames(Set<String> cacheNames) {
            Assert.notNull(cacheNames, "CacheNames must not be null!");

            cacheNames.forEach(it -> withCacheConfiguration(it, defaultCacheConfiguration));

            return this;
        }

        public TarantoolCacheManagerBuilder withInitialCacheConfigurations(Map<String, TarantoolCacheConfiguration> cacheConfigurations) {
            Assert.notNull(cacheConfigurations, "CacheConfigurations must not be null");

            cacheConfigurations.forEach((cacheName, configuration) -> Assert.notNull(configuration, String.format("TarantoolCacheConfiguration for cache %s must not be null!", cacheName)));

            this.initialCaches.putAll(cacheConfigurations);

            return this;
        }

        public TarantoolCacheManagerBuilder withCacheConfiguration(String cacheName, TarantoolCacheConfiguration cacheConfiguration) {
            Assert.notNull(cacheName, "CacheName must not be null");
            Assert.notNull(cacheConfiguration, "CacheConfiguration must not be null");

            this.initialCaches.put(cacheName, cacheConfiguration);

            return this;
        }

        public TarantoolCacheManagerBuilder disableCreateOnMissingCache() {
            this.allowInFlightCacheCreation = false;
            return this;
        }

        public Set<String> getConfiguredCaches() {
            return Collections.unmodifiableSet(this.initialCaches.keySet());
        }

        public Optional<TarantoolCacheConfiguration> getCacheConfigurationFor(String cacheName) {
            return Optional.ofNullable(this.initialCaches.get(cacheName));
        }

        public TarantoolCacheManager build() {
            return new TarantoolCacheManager(tarantoolClient, tarantoolConverter, defaultCacheConfiguration, initialCaches, allowInFlightCacheCreation);
        }
    }
}
