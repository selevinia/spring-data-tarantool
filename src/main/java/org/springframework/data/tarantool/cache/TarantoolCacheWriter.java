package org.springframework.data.tarantool.cache;

/**
 * {@link TarantoolCacheWriter} provides low level access to Tarantool space operations used for
 * caching.
 * The {@link TarantoolCacheWriter} may be shared by multiple cache implementations and is responsible for writing / reading
 * data to / from Tarantool.
 *
 * @author Tatiana Blinova
 */
public interface TarantoolCacheWriter {
}
