package org.springframework.data.tarantool.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.ReactiveTarantoolTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringJUnitConfig
public class AbstractReactiveTarantoolConfigurationTest {

    @Configuration
    static class Config extends AbstractReactiveTarantoolConfiguration {

    }

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextShouldContainTemplate() {
        assertThat(context.getBean(ReactiveTarantoolOperations.class)).isNotNull();
        assertThat(context.getBean(ReactiveTarantoolTemplate.class)).isNotNull();
    }

}
