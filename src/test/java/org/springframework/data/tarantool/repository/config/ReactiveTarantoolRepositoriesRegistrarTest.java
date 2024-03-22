package org.springframework.data.tarantool.repository.config;

import io.tarantool.driver.api.TarantoolClientConfig;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.mappers.DefaultMessagePackMapper;
import io.tarantool.driver.mappers.factories.DefaultMessagePackMapperFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.core.ReactiveTarantoolTemplate;
import org.springframework.data.tarantool.domain.User;
import org.springframework.data.tarantool.repository.MapIdReactiveTarantoolRepository;
import org.springframework.data.tarantool.repository.ReactiveTarantoolRepository;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.ComponentScan.Filter;

@SpringJUnitConfig
public class ReactiveTarantoolRepositoriesRegistrarTest {

    @Configuration
    @EnableReactiveTarantoolRepositories(basePackages = "org.springframework.data.tarantool.repository.config",
            considerNestedRepositories = true,
            includeFilters = @Filter(pattern = ".*ReactiveUserRepository", type = FilterType.REGEX))
    static class Config {

        @Bean
        @SuppressWarnings({"unchecked", "rawtypes"})
        public ReactiveTarantoolTemplate reactiveTarantoolTemplate() {
            DefaultMessagePackMapper messagePackMapper = DefaultMessagePackMapperFactory.getInstance().defaultComplexTypesMapper();

            TarantoolClient tarantoolClient = mock(TarantoolClient.class);
            when(tarantoolClient.getConfig()).thenReturn(TarantoolClientConfig.builder().withMessagePackMapper(messagePackMapper).build());

            return new ReactiveTarantoolTemplate(tarantoolClient);
        }
    }

    @Autowired
    private ReactiveUserRepository userRepository;

    @Autowired
    private MapIdReactiveUserRepository mapIdUserRepository;

    @Test
    void shouldStartConfiguration() {
        assertThat(userRepository).isNotNull();
        assertThat(mapIdUserRepository).isNotNull();
    }

    private interface ReactiveUserRepository extends ReactiveTarantoolRepository<User, String> {
    }

    private interface MapIdReactiveUserRepository extends MapIdReactiveTarantoolRepository<User> {
    }
}
