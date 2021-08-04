package org.springframework.data.tarantool.integration.repository;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.config.AbstractReactiveTarantoolConfiguration;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CartridgeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.integration.domain.DistributedUser;
import org.springframework.data.tarantool.repository.config.EnableReactiveTarantoolRepositories;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newDistributedUser;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newVersionedUser;

/**
 * Runner class for reactive repository tests for standard cartridge Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.cartridge.yml may be used.
 * To initialize cartridge after first run get terminal to tarantool-router-1 container and run /opt/integration-app/cluster-up.sh
 */
@SpringJUnitConfig
public class CartridgeReactiveTarantoolRepositoryTest extends AbstractReactiveTarantoolRepositoryTest {

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
        distributedUserRepository.save(user).as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        distributedUserRepository.findById(user.getId()).as(StepVerifier::create)
                .assertNext(actual -> {
                    assertThat(actual.getId()).isEqualTo(user.getId());
                    assertThat(actual.getBucketId()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldDoMassUpload() {
        userRepository.uploadUsers(List.of(newVersionedUser(), newVersionedUser())).as(StepVerifier::create)
                .expectNext(2L)
                .verifyComplete();
    }

}
