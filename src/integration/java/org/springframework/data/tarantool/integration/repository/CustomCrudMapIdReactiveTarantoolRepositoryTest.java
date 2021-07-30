package org.springframework.data.tarantool.integration.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.config.AbstractReactiveTarantoolConfiguration;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CustomCrudOperationsTarantoolClientOptions;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.repository.config.EnableReactiveTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

/**
 * Runner class for reactive repository tests with MapId for cartridge Tarantool installation with custom CRUD operation function implementations.
 * To run test cartridge using Docker, file docker-compose.cartridge.yml may be used.
 */
@SpringJUnitConfig
public class CustomCrudMapIdReactiveTarantoolRepositoryTest extends AbstractMapIdReactiveTarantoolRepositoryTest {

    @Configuration
    @EnableReactiveTarantoolRepositories(basePackages = "org.springframework.data.tarantool.integration.repository",
            considerNestedRepositories = true,
            includeFilters = @ComponentScan.Filter(pattern = ".*MapIdRepository", type = FilterType.REGEX))
    static class Config extends AbstractReactiveTarantoolConfiguration {

        @Bean
        @Override
        public TarantoolClientOptions tarantoolClientOptions() {
            return new CustomCrudOperationsTarantoolClientOptions();
        }

        @Override
        protected List<?> customConverters() {
            return List.of(new LocaleToStringConverter(), new StringToLocaleConverter());
        }
    }

}
