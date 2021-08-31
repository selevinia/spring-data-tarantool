package org.springframework.data.tarantool.cache;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Cache statistics for a {@link TarantoolCache}.
 *
 * @author Alexander Rublev
 */
public interface CacheStatistics {

	/**
	 * @return the name of the {@link TarantoolCache}.
	 */
	String getCacheName();

	/**
	 * @return number of put operations on the cache.
	 */
	long getPuts();

	/**
	 * @return the total number of get operations including both {@link #getHits() hits} and {@link #getMisses() misses}.
	 */
	long getGets();

	/**
	 * @return the number of cache get hits.
	 */
	long getHits();

	/**
	 * @return number of cache get misses.
	 */
	long getMisses();

	/**
	 * @return the number of {@link #getGets() gets} that have not yet been answered (neither {@link #getHits() hit} nor
	 *         {@link #getMisses() miss}).
	 */
	default long getPending() {
		return getGets() - (getHits() + getMisses());
	}

	/**
	 * @return number of cache removals.
	 */
	long getDeletes();

	/**
	 * @return initial point in time when started statistics capturing.
	 */
	Instant getSince();

	/**
	 * @return instantaneous point in time of last statistics counter reset. Equals {@link #getSince()} if never reset.
	 */
	Instant getLastReset();

	/**
	 * @return the statistics time.
	 */
	default Instant getTime() {
		return Instant.now();
	}
}
