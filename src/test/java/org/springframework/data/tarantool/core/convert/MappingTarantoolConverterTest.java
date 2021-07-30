package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.TarantoolTupleImpl;
import io.tarantool.driver.mappers.DefaultMessagePackMapperFactory;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.msgpack.value.ValueFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingTarantoolConverterTest extends AbstractConverterTest {

    private final MessagePackMapper messagePackMapper = DefaultMessagePackMapperFactory.getInstance().defaultComplexTypesMapper();
    private final TarantoolMappingContext mappingContext = new TarantoolMappingContext();

    private MappingTarantoolConverter mappingTarantoolConverter;

    @BeforeEach
    void setUp() {
        mappingTarantoolConverter = new MappingTarantoolConverter(mappingContext);
        mappingTarantoolConverter.afterPropertiesSet();
    }

    @Test
    void shouldGetMappingContext() {
        TarantoolMappingContext mappingContext = mappingTarantoolConverter.getMappingContext();
        assertThat(mappingContext).isNotNull();
        assertThat(mappingContext).isSameAs(this.mappingContext);
    }

    @Test
    void shouldGetDefaultMappingContext() {
        MappingTarantoolConverter mappingTarantoolConverter = new MappingTarantoolConverter();
        TarantoolMappingContext mappingContext = mappingTarantoolConverter.getMappingContext();
        assertThat(mappingContext).isNotNull();
        assertThat(mappingContext).isNotSameAs(this.mappingContext);
    }

    @Test
    void shouldReadValue() {
        Message message = new Message("1", "one");

        Message readMessage = mappingTarantoolConverter.read(Message.class, tuple(message));
        assertThat(readMessage).isEqualTo(message);
    }

    @Test
    void shouldWriteValue() {
        Message message = new Message("1", "one");
        TarantoolTuple tuple = emptyTuple();

        mappingTarantoolConverter.write(message, tuple);
        assertThat(tuple.getObject("id").orElse(null)).isEqualTo(message.id);
        assertThat(tuple.getObject("text").orElse(null)).isEqualTo(message.text);
    }

    private TarantoolTuple emptyTuple() {
        return new TarantoolTupleImpl(messagePackMapper, spaceMetadata());
    }

    private TarantoolTuple tuple(Message message) {
        return new TarantoolTupleImpl(Arrays.asList(ValueFactory.newString(message.id), ValueFactory.newString(message.text)), messagePackMapper, spaceMetadata());
    }

    private TarantoolSpaceMetadata spaceMetadata() {
        List<SpaceField> fields = List.of(
                SpaceField.of("id", "string", false),
                SpaceField.of("text", "string", false)
        );
        return spaceMetadata(0, "messages", fields);
    }

    @Space("messages")
    @Data
    @AllArgsConstructor
    private static class Message {
        @Id
        private String id;
        private String text;
    }
}
