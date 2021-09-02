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
 * @author Alexander Rublev
 * @see TarantoolCacheConfiguration
 * @see TarantoolCacheWriter
 */
public class TarantoolCacheManager extends AbstractCacheManager {
    private final TarantoolCacheWriter cacheWriter;
    private final TarantoolCacheConfiguration defaultCacheConfig;
    private final Map<String, TarantoolCacheConfiguration> initialCacheConfiguration;
    private final boolean allowInFlightCacheCreation;

    public TarantoolCacheManager(TarantoolCacheWriter cacheWriter,
                                 TarantoolCacheConfiguration defaultCacheConfig,
                                 boolean allowInFlightCacheCreation) {
        Assert.notNull(cacheWriter, "TarantoolCacheWriter must not be null");
        Assert.notNull(defaultCacheConfig, "DefaultCacheConfiguration must not be null");

        this.cacheWriter = cacheWriter;
        this.defaultCacheConfig = defaultCacheConfig;
        this.initialCacheConfiguration = new LinkedHashMap<>();
        this.allowInFlightCacheCreation = allowInFlightCacheCreation;
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWrite,
                                 TarantoolCacheConfiguration defaultCacheConfiguration) {
        this(cacheWrite, defaultCacheConfiguration, true);
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWrite,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 String... initialCacheNames) {
        this(cacheWrite, defaultCacheConfiguration, true, initialCacheNames);
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWrite,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 boolean allowInFlightCacheCreation,
                                 String... initialCacheNames) {
        this(cacheWrite, defaultCacheConfiguration, allowInFlightCacheCreation);

        for (String cacheName : initialCacheNames) {
            this.initialCacheConfiguration.put(cacheName, defaultCacheConfiguration);
        }
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWrite,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 Map<String, TarantoolCacheConfiguration> initialCacheConfigurations) {
        this(cacheWrite, defaultCacheConfiguration, initialCacheConfigurations, true);
    }

    public TarantoolCacheManager(TarantoolCacheWriter cacheWrite,
                                 TarantoolCacheConfiguration defaultCacheConfiguration,
                                 Map<String, TarantoolCacheConfiguration> initialCacheConfigurations,
                                 boolean allowInFlightCacheCreation) {
        this(cacheWrite, defaultCacheConfiguration, allowInFlightCacheCreation);

        Assert.notNull(initialCacheConfigurations, "InitialCacheConfigurations must not be null");

        this.initialCacheConfiguration.putAll(initialCacheConfigurations);
    }

    public static TarantoolCacheManager create(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                               TarantoolConverter tarantoolConverter) {
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");
        Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null");

        return new TarantoolCacheManager(new DefaultTarantoolCacheWriter(tarantoolClient, tarantoolConverter), TarantoolCacheConfiguration.defaultCacheConfig());
    }

    public static TarantoolCacheManagerBuilder builder() {
        return new TarantoolCacheManagerBuilder();
    }

    public static TarantoolCacheManagerBuilder builder(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                                       TarantoolConverter tarantoolConverter) {
        return new TarantoolCacheManagerBuilder(tarantoolClient, tarantoolConverter);
    }

    public static TarantoolCacheManagerBuilder builder(TarantoolCacheWriter cacheWriter) {
        return new TarantoolCacheManagerBuilder(cacheWriter);
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

    public static class TarantoolCacheManagerBuilder {
        private @Nullable TarantoolCacheWriter cacheWriter;
        private CacheStatisticsCollector statisticsCollector = CacheStatisticsCollector.none();
        private TarantoolCacheConfiguration defaultCacheConfiguration = TarantoolCacheConfiguration.defaultCacheConfig();
        private final Map<String, TarantoolCacheConfiguration> initialCaches = new LinkedHashMap<>();
        private boolean allowInFlightCacheCreation = true;

        private TarantoolCacheManagerBuilder() {
        }

        private TarantoolCacheManagerBuilder(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient, TarantoolConverter tarantoolConverter) {
            Assert.notNull(tarantoolClient, "TarantoolClient must not be null");
            Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null");

            this.cacheWriter = new DefaultTarantoolCacheWriter(tarantoolClient, tarantoolConverter);
        }

        private TarantoolCacheManagerBuilder(TarantoolCacheWriter cacheWriter) {
            Assert.notNull(cacheWriter, "TarantoolCacheWriter must not be null");

            this.cacheWriter = cacheWriter;
        }

        public TarantoolCacheManagerBuilder cacheDefaults(TarantoolCacheConfiguration defaultCacheConfiguration) {
            Assert.notNull(defaultCacheConfiguration, "DefaultCacheConfiguration must not be null");

            this.defaultCacheConfiguration = defaultCacheConfiguration;

            return this;
        }

        public TarantoolCacheManagerBuilder cacheWriter(TarantoolCacheWriter cacheWriter) {
            Assert.notNull(cacheWriter, "TarantoolCacheWriter must not be null");

            this.cacheWriter = cacheWriter;

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

        public TarantoolCacheManagerBuilder enableStatistics() {
            this.statisticsCollector = CacheStatisticsCollector.create();
            return this;
        }

        public TarantoolCacheManager build() {
            Assert.state(cacheWriter != null,
                    "CacheWriter must not be null! You can provide one via 'TarantoolCacheManagerBuilder#cacheWriter(TarantoolCacheWriter)'.");

            TarantoolCacheWriter theCacheWriter = cacheWriter;
            if (!statisticsCollector.equals(CacheStatisticsCollector.none())) {
                theCacheWriter = cacheWriter.withStatisticsCollector(statisticsCollector);
            }

            return new TarantoolCacheManager(theCacheWriter, defaultCacheConfiguration, initialCaches, allowInFlightCacheCreation);
        }
    }
}
