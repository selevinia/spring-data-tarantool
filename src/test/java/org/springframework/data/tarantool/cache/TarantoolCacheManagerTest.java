package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.metadata.TarantoolMetadataOperations;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.tarantool.cache.TarantoolCacheManager.TarantoolCacheManagerBuilder;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TarantoolCacheManagerTest {

    @Mock
    private TarantoolMetadataOperations metadata;

    @Mock
    private TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client;

    @Mock
    private TarantoolConverter converter;

    @Test
    void missingCacheShouldBeCreatedWithDefaultConfiguration() {
        when(metadata.getSpaceByName(any())).thenReturn(Optional.of(new TarantoolSpaceMetadata()));
        when(client.metadata()).thenReturn(metadata);

        TarantoolCacheConfiguration configuration = TarantoolCacheConfiguration.defaultCacheConfig().disableCachingNullValues();

        TarantoolCacheManager cm = TarantoolCacheManager.builder(client, converter).cacheDefaults(configuration).build();
        cm.afterPropertiesSet();

        assertThat(cm.getMissingCache("test-cache").getCacheConfiguration()).isEqualTo(configuration);
    }

    @Test
    void appliesDefaultConfigurationToInitialCache() {
        when(metadata.getSpaceByName(any())).thenReturn(Optional.of(new TarantoolSpaceMetadata()));
        when(client.metadata()).thenReturn(metadata);

        TarantoolCacheConfiguration withNulls = TarantoolCacheConfiguration.defaultCacheConfig();
        TarantoolCacheConfiguration withoutNulls = TarantoolCacheConfiguration.defaultCacheConfig().disableCachingNullValues();

        TarantoolCacheManager cm = TarantoolCacheManager.builder(client, converter).cacheDefaults(withNulls)
                .initialCacheNames(Collections.singleton("first-cache"))
                .cacheDefaults(withoutNulls)
                .initialCacheNames(Collections.singleton("second-cache"))
                .build();

        cm.afterPropertiesSet();

        assertThat(((TarantoolCache) cm.getCache("first-cache")).getCacheConfiguration()).isEqualTo(withNulls);
        assertThat(((TarantoolCache) cm.getCache("second-cache")).getCacheConfiguration()).isEqualTo(withoutNulls);
        assertThat(((TarantoolCache) cm.getCache("other-cache")).getCacheConfiguration()).isEqualTo(withoutNulls);
    }

    @Test
    void predefinedCacheShouldBeCreatedWithSpecificConfig() {
        when(metadata.getSpaceByName(any())).thenReturn(Optional.of(new TarantoolSpaceMetadata()));
        when(client.metadata()).thenReturn(metadata);

        TarantoolCacheConfiguration configuration = TarantoolCacheConfiguration.defaultCacheConfig().disableCachingNullValues();

        TarantoolCacheManager cm = TarantoolCacheManager.builder(client, converter)
                .withInitialCacheConfigurations(Collections.singletonMap("predefined-cache", configuration))
                .withInitialCacheConfigurations(Collections.singletonMap("another-predefined-cache", configuration))
                .build();
        cm.afterPropertiesSet();

        assertThat(((TarantoolCache) cm.getCache("predefined-cache")).getCacheConfiguration()).isEqualTo(configuration);
        assertThat(((TarantoolCache) cm.getCache("another-predefined-cache")).getCacheConfiguration()).isEqualTo(configuration);
        assertThat(cm.getMissingCache("new-cache").getCacheConfiguration()).isNotEqualTo(configuration);
    }

    @Test
    void lockedCacheManagerShouldPreventInFlightCacheCreation() {
        when(metadata.getSpaceByName(any())).thenReturn(Optional.of(new TarantoolSpaceMetadata()));
        when(client.metadata()).thenReturn(metadata);

        TarantoolCacheManager cm = TarantoolCacheManager.builder(client, converter)
                .initialCacheNames(Collections.singleton("configured"))
                .disableCreateOnMissingCache()
                .build();
        cm.afterPropertiesSet();

        assertThat(cm.getCache("not-configured")).isNull();
    }

    @Test
    void lockedCacheManagerShouldStillReturnPreconfiguredCaches() {
        when(metadata.getSpaceByName(any())).thenReturn(Optional.of(new TarantoolSpaceMetadata()));
        when(client.metadata()).thenReturn(metadata);

        TarantoolCacheManager cm = TarantoolCacheManager.builder(client, converter)
                .initialCacheNames(Collections.singleton("configured"))
                .disableCreateOnMissingCache()
                .build();
        cm.afterPropertiesSet();

        assertThat(cm.getCache("configured")).isNotNull();
    }

    @Test
    void cacheManagerBuilderReturnsConfiguredCaches() {
        TarantoolCacheManagerBuilder cmb = TarantoolCacheManager.builder(client, converter)
                .initialCacheNames(Collections.singleton("configured"))
                .disableCreateOnMissingCache();

        assertThat(cmb.getConfiguredCaches()).containsExactly("configured");
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> cmb.getConfiguredCaches().add("another"));
    }

    @Test
    void cacheManagerBuilderReturnsConfigurationForKnownCache() {
        TarantoolCacheManagerBuilder cmb = TarantoolCacheManager.builder(client, converter)
                .initialCacheNames(Collections.singleton("configured")).disableCreateOnMissingCache();

        assertThat(cmb.getCacheConfigurationFor("configured")).isPresent();
    }

    @Test
    void cacheManagerBuilderReturnsEmptyOptionalForUnknownCache() {
        TarantoolCacheManagerBuilder cmb = TarantoolCacheManager.builder(client, converter)
                .initialCacheNames(Collections.singleton("configured")).disableCreateOnMissingCache();

        assertThat(cmb.getCacheConfigurationFor("unknown")).isNotPresent();
    }

}
