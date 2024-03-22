package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.metadata.TarantoolSpaceMetadata;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.core.metadata.VSpaceToTarantoolSpaceMetadataConverter;
import io.tarantool.driver.core.tuple.TarantoolTupleImpl;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.mappers.factories.DefaultMessagePackMapperFactory;
import org.junit.jupiter.api.Test;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import org.springframework.data.mapping.Alias;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TarantoolTupleTypeAliasAccessorTest {

    private final MessagePackMapper messagePackMapper = DefaultMessagePackMapperFactory.getInstance().defaultComplexTypesMapper();

    private final String testTypeKey = "test_type_key";

    @Test
    void shouldWriteAndReadAlias() {
        TarantoolTupleTypeAliasAccessor accessor = new TarantoolTupleTypeAliasAccessor(testTypeKey);

        TarantoolTuple tuple = testTuple();
        assertThat(tuple).hasSize(0);

        String testTypeValue = "test_type_value";
        accessor.writeTypeTo(tuple, testTypeValue);
        assertThat(tuple).hasSize(1);
        assertThat(tuple.canGetObject(testTypeKey, String.class)).isTrue();
        assertThat((String) tuple.getObject(testTypeKey).orElse(null)).isEqualTo(testTypeValue);

        Alias alias = accessor.readAliasFrom(tuple);
        assertThat(alias.getValue()).isEqualTo(testTypeValue);
    }

    private TarantoolTuple testTuple() {
        return new TarantoolTupleImpl(messagePackMapper, testMetadata());
    }

    private TarantoolSpaceMetadata testMetadata() {
        Map<Value, Value> testFieldMap = new HashMap<>();
        testFieldMap.put(ValueFactory.newString("is_nullable"), ValueFactory.newBoolean(false));
        testFieldMap.put(ValueFactory.newString("name"), ValueFactory.newString(testTypeKey));
        testFieldMap.put(ValueFactory.newString("type"), ValueFactory.newString("string"));

        VSpaceToTarantoolSpaceMetadataConverter converter = VSpaceToTarantoolSpaceMetadataConverter.getInstance();
        return converter.fromValue(ValueFactory.newArray(
                ValueFactory.newInteger(0),             // spaceId
                ValueFactory.newInteger(0),             // ownerId
                ValueFactory.newString("space-name"),   // spaceName
                ValueFactory.newArray(ValueFactory.newMap(testFieldMap))
        ));
    }
}
