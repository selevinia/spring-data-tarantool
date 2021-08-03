package org.springframework.data.tarantool.core;

import io.tarantool.driver.exceptions.TarantoolClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.tarantool.core.mapping.event.BeforeSaveCallback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TarantoolTemplateTest extends AbstractTarantoolTemplateTest {
    private TarantoolTemplate tarantoolTemplate;

    @BeforeEach
    void setUp() {
        when(tarantoolClient.getConfig()).thenReturn(tarantoolClientConfig);

        EntityCallbacks callbacks = EntityCallbacks.create();
        callbacks.addEntityCallback((BeforeSaveCallback<Object>) (entity, tuple, spaceName) -> {
            assertThat(tuple).isNotNull();
            assertThat(spaceName).isNotNull();
            beforeSaveEntity = entity;
            return entity;
        });

        callbacks.addEntityCallback((BeforeConvertCallback<Object>) (entity, spaceName) -> {
            assertThat(spaceName).isNotNull();
            beforeConvertEntity = entity;
            return entity;
        });

        tarantoolTemplate = new TarantoolTemplate(tarantoolClient);
        tarantoolTemplate.setEntityCallbacks(callbacks);
    }

    @Test
    void shouldGetDefaultConverter() {
        TarantoolConverter converter = tarantoolTemplate.getConverter();
        assertThat(converter).isNotNull();
        assertThat(converter).isNotSameAs(customConverter);
        assertThat(converter.getMappingContext()).isNotNull();
    }

    @Test
    void shouldConfigureAndGetCustomConverter() {
        TarantoolTemplate template = new TarantoolTemplate(tarantoolClient, customConverter, new DefaultTarantoolExceptionTranslator());
        TarantoolConverter converter = template.getConverter();
        assertThat(converter).isNotNull();
        assertThat(converter).isSameAs(customConverter);
    }

    @Test
    void shouldTranslateException() {
        when(tarantoolClient.space(any())).thenThrow(new TarantoolClientException("Test exception"));

        assertThatThrownBy(() -> tarantoolTemplate.selectById("1", Message.class))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseInstanceOf(TarantoolClientException.class);
    }

}
