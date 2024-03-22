package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.core.metadata.VSpaceToTarantoolSpaceMetadataConverter;
import io.tarantool.driver.core.tuple.TarantoolTupleImpl;
import io.tarantool.driver.mappers.factories.DefaultMessagePackMapperFactory;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.api.metadata.TarantoolSpaceMetadata;
import lombok.*;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.tarantool.core.mapping.PrimaryKeyClass;
import org.springframework.data.tarantool.core.mapping.PrimaryKeyField;
import org.springframework.data.tarantool.core.mapping.Space;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractConverterTest {

    protected final MessagePackMapper messagePackMapper = DefaultMessagePackMapperFactory.getInstance().defaultComplexTypesMapper();

    protected TarantoolTuple allPossibleTypesEmptyTuple() {
        List<SpaceField> fields = List.of(
                SpaceField.of("idField", "string", false),
                SpaceField.of("stringField", "string", false),
                SpaceField.of("booleanField", "boolean", false),
                SpaceField.of("localDateTimeField", "number", false),
                SpaceField.of("localDateField", "number", false),
                SpaceField.of("localTimeField", "number", false),
                SpaceField.of("enumField", "string", false),
                SpaceField.of("integerField", "number", false),
                SpaceField.of("longField", "number", false),
                SpaceField.of("doubleField", "number", false),
                SpaceField.of("bigDecimalField", "number", false),
                SpaceField.of("uuidField", "uuid", false),
                SpaceField.of("objectField", "any", true),
                SpaceField.of("listOfLongField", "array", true),
                SpaceField.of("listOfObjectField", "array", true),
                SpaceField.of("mapOfStringField", "table", true),
                SpaceField.of("mapOfStringAndObjectField", "table", true),
                SpaceField.of("mapOfObjectAndStringField", "table", true)
        );
        return new TarantoolTupleImpl(messagePackMapper, spaceMetadata(0, "all_possible_types", fields));
    }

    protected TarantoolTuple withCompositePrimaryKeyEmptyTuple() {
        List<SpaceField> fields = List.of(
                SpaceField.of("id", "uuid", false),
                SpaceField.of("date", "number", false),
                SpaceField.of("text", "string", false)
        );
        return new TarantoolTupleImpl(messagePackMapper, spaceMetadata(1, "with_composite_primary_key", fields));
    }

    protected TarantoolTuple withValidConverterEmptyTuple() {
        List<SpaceField> fields = List.of(
                SpaceField.of("id", "string", false),
                SpaceField.of("_class", "string", false)
        );
        return new TarantoolTupleImpl(messagePackMapper, spaceMetadata(1, "with_valid_converter", fields));
    }

    protected TarantoolTuple withNotValidConverterEmptyTuple() {
        List<SpaceField> fields = List.of(
                SpaceField.of("id", "string", false),
                SpaceField.of("_class", "string", false)
        );
        return new TarantoolTupleImpl(messagePackMapper, spaceMetadata(2, "with_not_valid_converter", fields));
    }

    protected TarantoolSpaceMetadata spaceMetadata(Integer spaceId, String spaceName, List<SpaceField> fields) {
        List<MapValue> fieldsMap = fields.stream().map(field -> {
            Map<Value, Value> fieldMap = new HashMap<>();
            fieldMap.put(ValueFactory.newString("name"), ValueFactory.newString(field.name));
            fieldMap.put(ValueFactory.newString("type"), ValueFactory.newString(field.type));
            fieldMap.put(ValueFactory.newString("is_nullable"), ValueFactory.newBoolean(field.nullable));
            return ValueFactory.newMap(fieldMap);
        }).collect(Collectors.toList());

        VSpaceToTarantoolSpaceMetadataConverter converter = VSpaceToTarantoolSpaceMetadataConverter.getInstance();
        return converter.fromValue(ValueFactory.newArray(
                ValueFactory.newInteger(spaceId),
                ValueFactory.newInteger(spaceId),
                ValueFactory.newString(spaceName),
                ValueFactory.newArray(fieldsMap)
        ));
    }

    @Data(staticConstructor = "of")
    protected static class SpaceField {
        private final String name;
        private final String type;
        private final boolean nullable;
    }

    @Space("all_possible_types")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class AllPossibleTypes {
        @Id
        private String idField;
        private String stringField;
        private @Transient
        String transientStringField;
        private Boolean booleanField;
        private LocalDateTime localDateTimeField;
        private LocalDate localDateField;
        private LocalTime localTimeField;
        private TestEnum enumField;
        private Integer integerField;
        private Long longField;
        private Double doubleField;
        private BigDecimal bigDecimalField;
        private UUID uuidField;
        private AllPossibleTypes objectField;
        private List<Long> listOfLongField;
        private List<AllPossibleTypes> listOfObjectField;
        private Map<String, String> mapOfStringField;
        private Map<String, AllPossibleTypes> mapOfStringAndObjectField;
        private Map<AllPossibleTypes, String> mapOfObjectAndStringField;
    }

    @PrimaryKeyClass
    @Data
    @AllArgsConstructor
    protected static class CompositePrimaryKey {
        @PrimaryKeyField
        private UUID id;
        @PrimaryKeyField
        private LocalDate date;
    }

    @Space("with_composite_primary_key")
    @Data
    @AllArgsConstructor
    protected static class WithCompositePrimaryKey {
        @Id
        private CompositePrimaryKey key;
        private String text;
    }

    @Space("with_valid_converter")
    @Data
    @AllArgsConstructor
    protected static class WithValidConverter {
        @Id
        private String id;
    }

    @Space("with_not_valid_converter")
    @Data
    @AllArgsConstructor
    protected static class WithNotValidConverter {
        @Id
        private String id;
    }

    protected enum TestEnum {
        ONE, TWO
    }

    @WritingConverter
    protected class ValidWritingConverter implements Converter<WithValidConverter, TarantoolTuple> {
        @Override
        public TarantoolTuple convert(WithValidConverter source) {
            TarantoolTuple tuple = withValidConverterEmptyTuple();
            tuple.putObject("id", source.id);
            return tuple;
        }
    }

    @ReadingConverter
    protected static class ValidReadingConverter implements Converter<TarantoolTuple, WithValidConverter> {
        @Override
        public WithValidConverter convert(TarantoolTuple source) {
            return new WithValidConverter(source.getString("id"));
        }
    }

    @WritingConverter
    protected static class NotValidWritingConverter implements Converter<WithNotValidConverter, TarantoolTuple> {
        @Override
        public TarantoolTuple convert(WithNotValidConverter source) {
            return null;
        }
    }

    @ReadingConverter
    protected static class NotValidReadingConverter implements Converter<TarantoolTuple, WithNotValidConverter> {
        @Override
        public WithNotValidConverter convert(TarantoolTuple source) {
            return null;
        }
    }
}
