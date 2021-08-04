package org.springframework.data.tarantool.integration.repository;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.config.AbstractTarantoolConfiguration;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CartridgeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.integration.domain.DistributedUser;
import org.springframework.data.tarantool.repository.config.EnableTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newDistributedUser;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newVersionedUser;

/**
 * Runner class for repository tests for standard cartridge Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.cartridge.yml may be used.
 */
@SpringJUnitConfig
public class CartridgeTarantoolRepositoryTest extends AbstractTarantoolRepositoryTest {

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
            return new CartridgeTarantoolClientOptions();
        }

        @Override
        protected List<?> customConverters() {
            return List.of(new LocaleToStringConverter(), new StringToLocaleConverter());
        }
    }

    @Test
    void shouldDoSaveAndFindByIdDistributedEntity() {
        DistributedUser user = newDistributedUser();
        distributedUserRepository.save(user);

        Optional<DistributedUser> found = distributedUserRepository.findById(user.getId());
        assertWith(found.orElse(null), actual -> {
            assertThat(actual.getId()).isEqualTo(user.getId());
            assertThat(actual.getBucketId()).isNotNull();
        });
    }

    @Test
    void shouldDoMassUpload() {
        Long count = userRepository.uploadUsers(List.of(newVersionedUser(), newVersionedUser()));
        assertThat(count).isEqualTo(2L);
    }

}
