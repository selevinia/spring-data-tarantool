package org.springframework.data.tarantool.integration.repository;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.config.AbstractTarantoolConfiguration;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.integration.domain.User;
import org.springframework.data.tarantool.repository.Sort;
import org.springframework.data.tarantool.repository.config.EnableTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newUser;

/**
 * Runner class for repository tests for single node Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.single-node.yml may be used.
 */
@SpringJUnitConfig
public class SingleNodeTarantoolRepositoryTest extends AbstractTarantoolRepositoryTest {

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
            userRepository.save(user);
        }

        List<User> sortedAsc = userRepository.findAll(Sort.asc());
        List<User> sortedDesc = userRepository.findAll(Sort.desc());

        assertThat(sortedAsc.get(0)).isEqualTo(sortedDesc.get(3));
        assertThat(sortedAsc.get(1)).isEqualTo(sortedDesc.get(2));
        assertThat(sortedAsc.get(2)).isEqualTo(sortedDesc.get(1));
        assertThat(sortedAsc.get(3)).isEqualTo(sortedDesc.get(0));
    }

}
