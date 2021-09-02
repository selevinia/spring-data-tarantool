package org.springframework.data.tarantool.cache;

import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.Callable;

/**
 * {@link org.springframework.cache.Cache} implementation using for Tarantool as underlying store.
 * Use {@link TarantoolCacheManager} to create {@link TarantoolCache} instances.
 *
 * @author Tatiana Blinova
 * @author Alexander Rublev
 */
public class TarantoolCache extends AbstractValueAdaptingCache {

    private final String name;
    private final TarantoolCacheConfiguration cacheConfig;
    private final TarantoolCacheWriter cacheWriter;
    private final byte[] binaryNullValue;

    public TarantoolCache(String name, TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration cacheConfig) {
        super(cacheConfig.getAllowCacheNullValues());

        Assert.notNull(name, "Cache name must not be null");
        Assert.notNull(cacheWriter, "TarantoolCacheWriter must not be null");
        Assert.notNull(cacheConfig, "TarantoolCacheConfiguration must not be null");

        String preparedName = name;
        if (cacheConfig.getCacheNamePrefix() != null) {
            preparedName = String.format("%s_%s", cacheConfig.getCacheNamePrefix(), preparedName);
        }
        this.name = preparedName;
        this.cacheWriter = cacheWriter;
        this.cacheConfig = cacheConfig;
        this.binaryNullValue = cacheConfig.getSerializer().convert(NullValue.INSTANCE);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TarantoolCacheWriter getNativeCache() {
        return cacheWriter;
    }

    @Override
    protected Object lookup(Object key) {
        byte[] value = cacheWriter.get(name, serializeCacheKey(key));
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
        cacheWriter.put(name, serializeCacheKey(key), serializeCacheValue(toStoreValue(value)), cacheConfig.getTtl());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        byte[] result = cacheWriter.putIfAbsent(name, serializeCacheKey(key), serializeCacheValue(toStoreValue(value)), cacheConfig.getTtl());
        if (result == null) {
            return null;
        }
        return new SimpleValueWrapper(fromStoreValue(deserializeCacheValue(result)));
    }

    @Override
    public void evict(Object key) {
        cacheWriter.remove(name, serializeCacheKey(key));
    }

    @Override
    public void clear() {
        cacheWriter.clear(name);
    }

    /**
     * Get {@link TarantoolCacheConfiguration} used.
     *
     * @return immutable {@link TarantoolCacheConfiguration}. Never {@literal null}.
     */
    public TarantoolCacheConfiguration getCacheConfiguration() {
        return cacheConfig;
    }

    /**
     * Return the {@link CacheStatistics} snapshot for this cache instance. Statistics are accumulated per cache instance
     * and not from the backing Redis data store.
     *
     * @return statistics object for this {@link TarantoolCache}.
     */
    public CacheStatistics getStatistics() {
        return cacheWriter.getCacheStatistics(getName());
    }

    /**
     * Reset all statistics counters and gauges for this cache.
     */
    public void clearStatistics() {
        cacheWriter.clearStatistics(getName());
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
