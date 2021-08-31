package org.springframework.data.tarantool.cache;

import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

/**
 * Default mutable {@link CacheStatistics} implementation.
 *
 * @author Alexander Rublev
 */
class MutableCacheStatistics implements CacheStatistics {
	private final String cacheName;

	private final Instant aliveSince = Instant.now();
	private Instant lastReset = aliveSince;

	private final LongAdder puts = new LongAdder();
	private final LongAdder gets = new LongAdder();
	private final LongAdder hits = new LongAdder();
	private final LongAdder misses = new LongAdder();
	private final LongAdder deletes = new LongAdder();

	MutableCacheStatistics(String cacheName) {
		this.cacheName = cacheName;
	}

	@Override
	public String getCacheName() {
		return cacheName;
	}

	@Override
	public long getPuts() {
		return puts.sum();
	}

	void incPuts() {
		puts.increment();
	}

	@Override
	public long getGets() {
		return gets.sum();
	}

	void incGets() {
		gets.increment();
	}

	@Override
	public long getHits() {
		return hits.sum();
	}

	void incHits() {
		hits.increment();
	}

	@Override
	public long getMisses() {
		return misses.sum();
	}

	void incMisses() {
		misses.increment();
	}

	@Override
	public long getDeletes() {
		return deletes.sum();
	}

	/**
	 * @param x number of removals to add.
	 */
	void incDeletes(int x) {
		deletes.add(x);
	}

	@Override
	public Instant getSince() {
		return this.aliveSince;
	}

	@Override
	public Instant getLastReset() {
		return lastReset;
	}

	void reset() {
		lastReset = Instant.now();

		puts.reset();
		gets.reset();
		hits.reset();
		misses.reset();
		deletes.reset();
	}

	CacheStatistics captureSnapshot() {
		return new Snapshot(this);
	}

	/**
	 * {@link CacheStatistics} value object holding snapshot data.
	 */
	private static class Snapshot implements CacheStatistics {

		private final String cacheName;
		private final long puts;
		private final long gets;
		private final long hits;
		private final long misses;
		private final long deletes;
		private final long pending;
		private final Instant time;
		private final Instant since;
		private final Instant lastReset;

		Snapshot(CacheStatistics statistics) {

			cacheName = statistics.getCacheName();
			gets = statistics.getGets();
			hits = statistics.getHits();
			misses = statistics.getMisses();
			puts = statistics.getPuts();
			deletes = statistics.getDeletes();
			pending = gets - (hits + misses);

			time = Instant.now();
			since = Instant.from(statistics.getSince());
			lastReset = Instant.from(statistics.getLastReset());
		}

		@Override
		public String getCacheName() {
			return cacheName;
		}

		@Override
		public long getPuts() {
			return puts;
		}

		@Override
		public long getGets() {
			return gets;
		}

		@Override
		public long getHits() {
			return hits;
		}

		@Override
		public long getMisses() {
			return misses;
		}

		@Override
		public long getPending() {
			return pending;
		}

		@Override
		public long getDeletes() {
			return deletes;
		}

		@Override
		public Instant getSince() {
			return since;
		}

		@Override
		public Instant getLastReset() {
			return lastReset;
		}

		@Override
		public Instant getTime() {
			return time;
		}
	}
}
