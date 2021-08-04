package org.springframework.data.tarantool.config;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mapping.callback.ReactiveEntityCallbacks;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
public class ReactiveTarantoolAuditingTest {

    @Autowired
    private ApplicationContext context;

    @EnableReactiveTarantoolAuditing
    @Configuration
    static class Config {

        @Bean
        public TarantoolMappingContext mappingContext() {
            return new TarantoolMappingContext();
        }

        @Bean
        public TarantoolConverter tarantoolConverter(TarantoolMappingContext mappingContext) {
            return new MappingTarantoolConverter(mappingContext);
        }

        @Bean
        public ReactiveAuditorAware<String> auditorAware() {
            return () -> Mono.just("Alexey Kuzin");
        }
    }

    @Test
    void enablesAuditingAndSetsPropertiesAccordingly() {
        TarantoolMappingContext mappingContext = context.getBean(TarantoolMappingContext.class);
        mappingContext.getPersistentEntity(TestEntity.class);

        ReactiveEntityCallbacks callbacks = ReactiveEntityCallbacks.create(context);

        TestEntity entity = new TestEntity();
        callbacks.callback(ReactiveBeforeConvertCallback.class, entity, "test").as(StepVerifier::create)
                .consumeNextWith(actual -> {
                    assertThat(actual.created).isNotNull();
                    assertThat(actual.createdBy).isEqualTo("Alexey Kuzin");
                    assertThat(actual.modified).isNotNull();
                }).verifyComplete();
    }

    @Data
    @Space("test")
    private static class TestEntity {
        @Id
        private Long id;

        @CreatedDate
        private LocalDateTime created;

        @CreatedBy
        private String createdBy;

        @LastModifiedDate
        private LocalDateTime modified;
    }
}
