package org.springframework.data.tarantool.integration.repository.mapped;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.config.AbstractTarantoolConfiguration;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CartridgeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.repository.config.EnableTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

/**
 * Runner class for repository tests with MapId for standard cartridge Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.cartridge.yml may be used.
 * To initialize cartridge after first run get terminal to tarantool-router-1 container and run /opt/integration-app/cluster-up.sh
 */
@SpringJUnitConfig
public class CartridgeMapIdTarantoolRepositoryTest extends AbstractMapIdTarantoolRepositoryTest {

    @Configuration
    @EnableTarantoolRepositories(basePackages = "org.springframework.data.tarantool.integration.repository",
            considerNestedRepositories = true,
            includeFilters = @ComponentScan.Filter(pattern = ".*MapIdRepository", type = FilterType.REGEX))
    static class Config extends AbstractTarantoolConfiguration {

        @Bean
        @Override
        public TarantoolClientOptions tarantoolClientOptions() {
            return new CartridgeTarantoolClientOptions();
        }

        @Override
        protected List<?> customConverters() {
            return List.of(new LocaleToStringConverter(), new StringToLocaleConverter());
        }
    }

}
