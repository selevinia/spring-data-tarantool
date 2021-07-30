package org.springframework.data.tarantool.integration.repository;

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
import org.springframework.data.tarantool.integration.domain.User;
import org.springframework.data.tarantool.repository.Sort;
import org.springframework.data.tarantool.repository.config.EnableReactiveTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runner class for reactive repository tests for single node Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.single-node.yml may be used.
 */
@SpringJUnitConfig
public class SingleNodeReactiveTarantoolRepositoryTest extends AbstractReactiveTarantoolRepositoryTest {

    @Configuration
    @EnableReactiveTarantoolRepositories(basePackages = "org.springframework.data.tarantool.integration.repository",
            considerNestedRepositories = true,
            includeFilters = {
                    @ComponentScan.Filter(pattern = ".*UserRepository", type = FilterType.REGEX),
                    @ComponentScan.Filter(pattern = ".*ArticleRepository", type = FilterType.REGEX)
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
    void shouldFindAllSorted() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            userRepository.save(user).as(StepVerifier::create)
                    .expectNext(user)
                    .verifyComplete();
        }

        List<User> sortedAsc = new ArrayList<>();
        userRepository.findAll(Sort.asc()).as(StepVerifier::create)
                .assertNext(sortedAsc::add)
                .assertNext(sortedAsc::add)
                .assertNext(sortedAsc::add)
                .assertNext(sortedAsc::add)
                .verifyComplete();

        List<User> sortedDesc = new ArrayList<>();
        userRepository.findAll(Sort.desc()).as(StepVerifier::create)
                .assertNext(sortedDesc::add)
                .assertNext(sortedDesc::add)
                .assertNext(sortedDesc::add)
                .assertNext(sortedDesc::add)
                .verifyComplete();

        assertThat(sortedAsc.get(0)).isEqualTo(sortedDesc.get(3));
        assertThat(sortedAsc.get(1)).isEqualTo(sortedDesc.get(2));
        assertThat(sortedAsc.get(2)).isEqualTo(sortedDesc.get(1));
        assertThat(sortedAsc.get(3)).isEqualTo(sortedDesc.get(0));
    }

}
