package org.springframework.data.tarantool.cache;

import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;

/**
 * {@link org.springframework.cache.Cache} implementation using for Tarantool as underlying store.
 * Use {@link TarantoolCacheManager} to create {@link TarantoolCache} instances.
 *
 * @author Tatiana Blinova
 */
public class TarantoolCache extends AbstractValueAdaptingCache {

    private final String name;
    private final TarantoolCacheWriter cacheWriter;
    private final TarantoolCacheConfiguration cacheConfig;

    public TarantoolCache(String name, TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration cacheConfig) {
        super(cacheConfig.getAllowCacheNullValues());

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(cacheWriter, "CacheWriter must not be null");
        Assert.notNull(cacheConfig, "CacheConfig must not be null");

        this.name = name;
        this.cacheWriter = cacheWriter;
        this.cacheConfig = cacheConfig;
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
        return cacheWriter.get(name, key);
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
        cacheWriter.put(name, key, toStoreValue(value), cacheConfig.getTtl());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Object result = cacheWriter.putIfAbsent(name, key, toStoreValue(value), cacheConfig.getTtl());
        return new SimpleValueWrapper(fromStoreValue(result));
    }

    @Override
    public void evict(Object key) {
        cacheWriter.remove(name, key);
    }

    @Override
    public void clear() {
        cacheWriter.remove(name);
    }
}
