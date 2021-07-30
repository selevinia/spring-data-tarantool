package org.springframework.data.tarantool.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.ReactiveIsNewAwareAuditingHandler;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactiveTarantoolAuditingRegistrarTest {

    @EnableReactiveTarantoolAuditing
    static class Config {

        @Bean
        TarantoolConverter tarantoolConverter() {
            return new MappingTarantoolConverter();
        }
    }

    @Test
    void shouldRegisterPersistentEntitiesOnce() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setAllowBeanDefinitionOverriding(false);
        context.register(Config.class);

        context.refresh();

        assertThat(context.getBean(ReactiveIsNewAwareAuditingHandler.class)).isNotNull();

        context.stop();
    }

}
