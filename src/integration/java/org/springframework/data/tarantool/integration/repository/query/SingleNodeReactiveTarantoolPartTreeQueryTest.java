package org.springframework.data.tarantool.integration.repository.query;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.config.AbstractReactiveTarantoolConfiguration;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.integration.repository.query.CartridgeReactiveTarantoolPartTreeTest;
import org.springframework.data.tarantool.repository.config.EnableReactiveTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import java.util.List;

/**
 * Runner class for reactive repository tests for single node Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.single-node.yml may be used.
 */
@SpringJUnitConfig
public class SingleNodeReactiveTarantoolPartTreeQueryTest extends CartridgeReactiveTarantoolPartTreeTest {

    @Configuration
    @EnableReactiveTarantoolRepositories(basePackages = "org.springframework.data.tarantool.integration.repository",
            considerNestedRepositories = true,
            includeFilters = {
                    @ComponentScan.Filter(pattern = ".*UserRepository", type = FilterType.REGEX)
            })
    static class Config extends AbstractReactiveTarantoolConfiguration {

        @Bean
        @Override
        public TarantoolClientOptions tarantoolClientOptions() {
            return new SingleNodeTarantoolClientOptions();
        }

        @Override
        protected List<?> customConverters() {
            return List.of(new LocaleToStringConverter(), new StringToLocaleConverter());
        }
    }

    @Test
    void shouldFindAllByAgeBetween() {
        userRepository.findAllByAgeBetween(20, 23).as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable instanceof UnsupportedOperationException)
                .verify();
    }
}
