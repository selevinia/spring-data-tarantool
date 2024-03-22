package org.springframework.data.tarantool.repository.config;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolClientConfig;
import io.tarantool.driver.mappers.DefaultMessagePackMapper;
import io.tarantool.driver.mappers.factories.DefaultMessagePackMapperFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.tarantool.core.TarantoolTemplate;
import org.springframework.data.tarantool.domain.User;
import org.springframework.data.tarantool.repository.MapIdTarantoolRepository;
import org.springframework.data.tarantool.repository.TarantoolRepository;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.ComponentScan.Filter;

@SpringJUnitConfig
public class TarantoolRepositoriesRegistrarTest {

    @Configuration
    @EnableTarantoolRepositories(basePackages = "org.springframework.data.tarantool.repository.config",
            considerNestedRepositories = true,
            includeFilters = @Filter(pattern = ".*UserRepository", type = FilterType.REGEX))
    static class Config {

        @Bean
        @SuppressWarnings({"unchecked", "rawtypes"})
        public TarantoolTemplate tarantoolTemplate() {
            DefaultMessagePackMapper messagePackMapper = DefaultMessagePackMapperFactory.getInstance().defaultComplexTypesMapper();

            TarantoolClient tarantoolClient = mock(TarantoolClient.class);
            when(tarantoolClient.getConfig()).thenReturn(TarantoolClientConfig.builder().withMessagePackMapper(messagePackMapper).build());

            return new TarantoolTemplate(tarantoolClient);
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MapIdUserRepository mapIdUserRepository;

    @Test
    void shouldStartConfiguration() {
        assertThat(userRepository).isNotNull();
        assertThat(mapIdUserRepository).isNotNull();
    }

    private interface UserRepository extends TarantoolRepository<User, String> {
    }

    private interface MapIdUserRepository extends MapIdTarantoolRepository<User> {
    }
}
