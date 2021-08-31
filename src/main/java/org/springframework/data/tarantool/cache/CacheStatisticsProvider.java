package org.springframework.data.tarantool.cache;

/**
 * Interface to be implemented by objects that expose {@link CacheStatistics} identified by {@code cacheName}. Typically,
 * used by cache writers.
 *
 * @author Alexander Rublev
 */
public interface CacheStatisticsProvider {

	/**
	 * Obtain snapshot of the captured statistics. May return a statistics object whose counters are zero if there are no
	 * statistics for {@code cacheName}.
	 *
	 * @param cacheName must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	CacheStatistics getCacheStatistics(String cacheName);
}
