package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.Callable;

/**
 * {@link org.springframework.cache.Cache} implementation using for Tarantool as underlying store.
 * Use {@link TarantoolCacheManager} to create {@link TarantoolCache} instances.
 *
 * @author Tatiana Blinova
 */
public class TarantoolCache extends AbstractValueAdaptingCache {

    private final String cacheName;
    private final TarantoolCacheConfiguration cacheConfig;
    private final TarantoolNativeCache nativeCache;
    private final byte[] binaryNullValue;

    public TarantoolCache(String cacheName, TarantoolCacheConfiguration cacheConfig,
                          TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                          TarantoolConverter tarantoolConverter,
                          CacheStatisticsCollector statisticsCollector) {
        super(cacheConfig.getAllowCacheNullValues());

        Assert.notNull(cacheName, "Cache name must not be null");
        Assert.notNull(cacheConfig, "CacheConfig must not be null");
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");
        Assert.notNull(statisticsCollector, "CacheStatisticsCollector must not be null");

        this.cacheName = cacheName;
        this.cacheConfig = cacheConfig;
        this.nativeCache = new DefaultTarantoolNativeCache(cacheName, cacheConfig.getCacheNamePrefix(), tarantoolClient, tarantoolConverter, statisticsCollector);
        this.binaryNullValue = cacheConfig.getSerializer().convert(NullValue.INSTANCE);
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public TarantoolNativeCache getNativeCache() {
        return nativeCache;
    }

    @Override
    protected Object lookup(Object key) {
        byte[] value = nativeCache.get(serializeCacheKey(key));
        if (value == null) {
            return null;
        }
        return deserializeCacheValue(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper result = get(key);

        if (result != null) {
            return (T) result.get();
        }

        T value;
        try {
            value = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
        put(key, value);
        return value;
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        nativeCache.put(serializeCacheKey(key), serializeCacheValue(toStoreValue(value)), cacheConfig.getTtl());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        byte[] result = nativeCache.putIfAbsent(serializeCacheKey(key), serializeCacheValue(toStoreValue(value)), cacheConfig.getTtl());
        if (result == null) {
            return null;
        }
        return new SimpleValueWrapper(fromStoreValue(deserializeCacheValue(result)));
    }

    @Override
    public void evict(Object key) {
        nativeCache.remove(serializeCacheKey(key));
    }

    @Override
    public void clear() {
        nativeCache.remove();
    }

    /**
     * Return the {@link CacheStatistics} snapshot for this cache instance. Statistics are accumulated per cache instance
     * and not from the backing Redis data store.
     *
     * @return statistics object for this {@link TarantoolCache}.
     */
    public CacheStatistics getStatistics() {
        return nativeCache.getCacheStatistics(getName());
    }

    /**
     * Reset all statistics counters and gauges for this cache.
     */
    public void clearStatistics() {
        nativeCache.clearStatistics(getName());
    }

    /**
     * Get {@link TarantoolCacheConfiguration} used.
     *
     * @return immutable {@link TarantoolCacheConfiguration}. Never {@literal null}.
     */
    public TarantoolCacheConfiguration getCacheConfiguration() {
        return cacheConfig;
    }

    private byte[] serializeCacheKey(Object cacheKey) {
        return cacheConfig.getSerializer().convert(cacheKey);
    }

    private byte[] serializeCacheValue(Object value) {
        if (isAllowNullValues() && value instanceof NullValue) {
            return binaryNullValue;
        }
        return cacheConfig.getSerializer().convert(value);
    }

    private Object deserializeCacheValue(byte[] value) {
        if (isAllowNullValues() && ObjectUtils.nullSafeEquals(value, binaryNullValue)) {
            return NullValue.INSTANCE;
        }
        return cacheConfig.getDeserializer().convert(value);
    }
}
