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
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.event.BeforeConvertCallback;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
public class TarantoolAuditingTest {

    @Autowired
    private ApplicationContext context;

    @EnableTarantoolAuditing
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
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("Alexey Kuzin");
        }
    }

    @Test
    void enablesAuditingAndSetsPropertiesAccordingly() {
        TarantoolMappingContext mappingContext = context.getBean(TarantoolMappingContext.class);
        mappingContext.getPersistentEntity(TestEntity.class);

        EntityCallbacks callbacks = EntityCallbacks.create(context);

        TestEntity entity = new TestEntity();
        TestEntity actual = callbacks.callback(BeforeConvertCallback.class, entity, "test");
        assertThat(actual.created).isNotNull();
        assertThat(actual.createdBy).isEqualTo("Alexey Kuzin");
        assertThat(actual.modified).isNotNull();
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
