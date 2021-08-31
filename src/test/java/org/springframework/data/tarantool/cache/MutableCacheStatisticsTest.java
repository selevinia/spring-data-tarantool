package org.springframework.data.tarantool.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MutableCacheStatisticsTest {
	MutableCacheStatistics statistics = new MutableCacheStatistics("test-cache");

	@Test
	void shouldReportRetrievals() {
		assertThat(statistics.getGets()).isZero();

		statistics.incGets();

		assertThat(statistics.getGets()).isOne();
	}

	@Test
	void shouldReportHits() {
		assertThat(statistics.getHits()).isZero();

		statistics.incHits();

		assertThat(statistics.getHits()).isOne();
	}

	@Test
	void shouldReportMisses() {
		assertThat(statistics.getMisses()).isZero();

		statistics.incMisses();

		assertThat(statistics.getMisses()).isOne();
	}

	@Test
	void shouldReportPuts() {
		assertThat(statistics.getPuts()).isZero();

		statistics.incPuts();

		assertThat(statistics.getPuts()).isOne();
	}

	@Test
	void shouldReportRemovals() {
		assertThat(statistics.getDeletes()).isZero();

		statistics.incDeletes(1);

		assertThat(statistics.getDeletes()).isOne();
	}
}
