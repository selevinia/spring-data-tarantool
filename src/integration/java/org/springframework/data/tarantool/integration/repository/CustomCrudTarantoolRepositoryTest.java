package org.springframework.data.tarantool.integration.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.config.AbstractTarantoolConfiguration;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CustomCrudOperationsTarantoolClientOptions;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.integration.repository.util.CaptureEventListener;
import org.springframework.data.tarantool.repository.config.EnableTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

/**
 * Runner class for repository tests for cartridge Tarantool installation with custom CRUD operation function implementations.
 * To run test cartridge using Docker, file docker-compose.cartridge.yml may be used.
 * To initialize cartridge after first run get terminal to tarantool-router-1 container and run /opt/integration-app/cluster-up.sh
 */
@SpringJUnitConfig
public class CustomCrudTarantoolRepositoryTest extends AbstractTarantoolRepositoryTest {

    @Configuration
    @EnableTarantoolRepositories(basePackages = "org.springframework.data.tarantool.integration.repository",
            considerNestedRepositories = true,
            includeFilters = {
                    @ComponentScan.Filter(pattern = ".*UserRepository", type = FilterType.REGEX),
                    @ComponentScan.Filter(pattern = ".*ArticleRepository", type = FilterType.REGEX)
            })
    static class Config extends AbstractTarantoolConfiguration {

        @Bean
        @Override
        public TarantoolClientOptions tarantoolClientOptions() {
            return new CustomCrudOperationsTarantoolClientOptions();
        }

        @Override
        protected List<?> customConverters() {
            return List.of(new LocaleToStringConverter(), new StringToLocaleConverter());
        }

        @Bean
        public CaptureEventListener eventListener() {
            return new CaptureEventListener();
        }
    }

}
