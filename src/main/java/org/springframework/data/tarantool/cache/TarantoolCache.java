package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
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

    private final String cacheName;
    private final TarantoolCacheConfiguration cacheConfig;
    private final TarantoolNativeCache nativeCache;

    public TarantoolCache(String cacheName, TarantoolCacheConfiguration cacheConfig,
                          TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                          TarantoolConverter tarantoolConverter) {
        super(cacheConfig.getAllowCacheNullValues());

        Assert.notNull(cacheName, "Cache name must not be null");
        Assert.notNull(cacheConfig, "CacheConfig must not be null");
        Assert.notNull(tarantoolClient, "TarantoolClient must not be null");

        this.cacheName = cacheName;
        this.cacheConfig = cacheConfig;
        this.nativeCache = new DefaultTarantoolNativeCache(cacheName, tarantoolClient, tarantoolConverter);
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
        return nativeCache.get(key);
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
        nativeCache.put(key, toStoreValue(value), cacheConfig.getTtl());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Object result = nativeCache.putIfAbsent(key, toStoreValue(value), cacheConfig.getTtl());
        return new SimpleValueWrapper(fromStoreValue(result));
    }

    @Override
    public void evict(Object key) {
        nativeCache.remove(key);
    }

    @Override
    public void clear() {
        nativeCache.remove(cacheName);
    }
}
