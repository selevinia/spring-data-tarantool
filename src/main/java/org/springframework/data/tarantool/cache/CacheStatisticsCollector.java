package org.springframework.data.tarantool.cache;

/**
 * The statistics collector supports capturing of relevant {@link TarantoolCache} operations such as
 * {@literal hits & misses}.
 *
 * @author Alexander Rublev
 */
public interface CacheStatisticsCollector extends CacheStatisticsProvider {

	/**
	 * Increase the counter for {@literal put operations} of the given cache.
	 *
	 * @param cacheName must not be {@literal null}.
	 */
	void incPuts(String cacheName);

	/**
	 * Increase the counter for {@literal get operations} of the given cache.
	 *
	 * @param cacheName must not be {@literal null}.
	 */
	void incGets(String cacheName);

	/**
	 * Increase the counter for {@literal get operations with result} of the given cache.
	 *
	 * @param cacheName must not be {@literal null}.
	 */
	void incHits(String cacheName);

	/**
	 * Increase the counter for {@literal get operations without result} of the given cache.
	 *
	 * @param cacheName must not be {@literal null}.
	 */
	void incMisses(String cacheName);

	/**
	 * Increase the counter for {@literal delete operations} of the given cache.
	 *
	 * @param cacheName must not be {@literal null}.
	 */
	default void incDeletes(String cacheName) {
		incDeletesBy(cacheName, 1);
	}

	/**
	 * Increase the counter for {@literal delete operations} of the given cache by the given value.
	 *
	 * @param cacheName must not be {@literal null}.
	 * @param value number of delete actions
	 */
	void incDeletesBy(String cacheName, int value);

	/**
	 * Reset the all counters and gauges of for the given cache.
	 *
	 * @param cacheName must not be {@literal null}.
	 */
	void reset(String cacheName);

	/**
	 * @return a {@link CacheStatisticsCollector} that performs no action.
	 */
	static CacheStatisticsCollector none() {
		return NoOpCacheStatisticsCollector.INSTANCE;
	}

	/**
	 * @return a default {@link CacheStatisticsCollector} implementation.
	 */
	static CacheStatisticsCollector create() {
		return new DefaultCacheStatisticsCollector();
	}
}
