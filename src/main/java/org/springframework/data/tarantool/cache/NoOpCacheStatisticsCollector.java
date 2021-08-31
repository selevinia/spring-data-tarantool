package org.springframework.data.tarantool.cache;

import org.springframework.util.ObjectUtils;

import java.time.Instant;

/**
 * {@link CacheStatisticsCollector} implementation that does not capture anything end return
 * {@link EmptyStatistics} when {@link #getCacheStatistics(String) obtaining} {@link CacheStatistics} for a cache.
 *
 * @author Alexander Rublev
 */
enum NoOpCacheStatisticsCollector implements CacheStatisticsCollector {

	INSTANCE;

	@Override
	public void incPuts(String cacheName) {}

	@Override
	public void incGets(String cacheName) {}

	@Override
	public void incHits(String cacheName) {}

	@Override
	public void incMisses(String cacheName) {}

	@Override
	public void incDeletesBy(String cacheName, int value) {}

	@Override
	public void reset(String cacheName) {}

	@Override
	public CacheStatistics getCacheStatistics(String cacheName) {
		return new EmptyStatistics(cacheName);
	}

	private static class EmptyStatistics implements CacheStatistics {

		private final String cacheName;

		EmptyStatistics(String cacheName) {
			this.cacheName = cacheName;
		}

		@Override
		public String getCacheName() {
			return cacheName;
		}

		@Override
		public long getPuts() {
			return 0;
		}

		@Override
		public long getGets() {
			return 0;
		}

		@Override
		public long getHits() {
			return 0;
		}

		@Override
		public long getMisses() {
			return 0;
		}

		@Override
		public long getDeletes() {
			return 0;
		}

		@Override
		public Instant getSince() {
			return Instant.EPOCH;
		}

		@Override
		public Instant getLastReset() {
			return getSince();
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			EmptyStatistics that = (EmptyStatistics) o;
			return ObjectUtils.nullSafeEquals(cacheName, that.cacheName);
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(cacheName);
		}
	}
}
