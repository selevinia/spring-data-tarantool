package org.springframework.data.tarantool.cache;

import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * {@link TarantoolNativeCache} provides low level access to Tarantool space operations used for
 * caching.
 *
 * @author Tatiana Blinova
 */
public interface TarantoolNativeCache extends CacheStatisticsProvider {

    /**
     * Get value from Tarantool stored for the given key.
     *
     * @param key must not be {@literal null}.
     * @return {@literal null} if key does not exist.
     */
    @Nullable
    byte[] get(byte[] key);

    /**
     * Write the given key/value pair to Tarantool and set the expiration time if defined.
     *
     * @param key   The key for the cache entry. Must not be {@literal null}.
     * @param value The value stored for the key. Must not be {@literal null}.
     * @param ttl   Optional expiration time. Can be {@literal null}.
     */
    void put(byte[] key, byte[] value, @Nullable Duration ttl);

    /**
     * Write the given value to Tarantool if the key does not already exist.
     *
     * @param key   The key for the cache entry. Must not be {@literal null}.
     * @param value The value stored for the key. Must not be {@literal null}.
     * @param ttl   Optional expiration time. Can be {@literal null}.
     * @return {@literal null} if the value has been written, the value stored for the key if it already exists.
     */
    @Nullable
    byte[] putIfAbsent(byte[] key, byte[] value, @Nullable Duration ttl);

    /**
     * Remove the given key from Tarantool.
     *
     * @param key The key for the cache entry. Must not be {@literal null}.
     */
    void remove(byte[] key);

    /**
     * Remove all keys.
     */
    void remove();
}
