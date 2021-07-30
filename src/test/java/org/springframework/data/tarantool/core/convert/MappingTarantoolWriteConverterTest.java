package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MappingTarantoolWriteConverterTest extends AbstractConverterTest {

    private final TarantoolMappingContext mappingContext = new TarantoolMappingContext();
    private final TypeMapper<TarantoolTuple> tupleTypeMapper = new TarantoolTupleTypeMapper();
    private final TypeMapper<Map<String, Object>> mapTypeMapper = new TarantoolMapTypeMapper();
    private final GenericConversionService conversionService = new GenericConversionService();
    private final CustomConversions customConversions = new TarantoolCustomConversions(List.of(new ValidWritingConverter(), new NotValidWritingConverter()));

    private TarantoolWriteConverter writeConverter;

    @BeforeEach
    void setUp() {
        DefaultConversionService.addCollectionConverters(conversionService);
        customConversions.registerConvertersIn(conversionService);
        writeConverter = new MappingTarantoolWriteConverter(mappingContext, tupleTypeMapper, mapTypeMapper, () -> customConversions, conversionService);
    }

    @Test
    void shouldNotWriteWithIncorrectTarget() {
        assertThatThrownBy(() -> writeConverter.write(LocalDateTime.now(), 1L))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("Unknown write target");

        assertThatThrownBy(() -> writeConverter.write(LocalDateTime.now(), List.of(1L)))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("Unknown write target");

        assertThatThrownBy(() -> writeConverter.write(List.of(1L), LocalDateTime.now()))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("Unknown write target");
    }

    @Test
    void shouldWriteEmptyObject() {
        AllPossibleTypes source = AllPossibleTypes.builder().build();
        TarantoolTuple tuple = allPossibleTypesEmptyTuple();

        writeConverter.write(source, tuple);
        assertThat(tuple).isNotNull();
        assertThat(tuple).hasSize(0);
    }

    @Test
    void shouldWriteObject() {
        AllPossibleTypes source = AllPossibleTypes.builder()
                .idField("id1")
                .stringField("string")
                .transientStringField("transient")
                .booleanField(true)
                .localDateTimeField(LocalDateTime.now())
                .localDateField(LocalDate.now())
                .localTimeField(LocalTime.MIDNIGHT)
                .enumField(TestEnum.ONE)
                .integerField(1)
                .longField(1_000_000_000_001L)
                .doubleField(1.2)
                .bigDecimalField(new BigDecimal("1.2"))
                .uuidField(UUID.randomUUID())
                .build();
        TarantoolTuple tuple = allPossibleTypesEmptyTuple();

        writeConverter.write(source, tuple);
        assertThat(tuple).isNotNull().hasSize(12);
        assertThat(tuple.getObject("idField", String.class)).contains(source.getIdField());
        assertThat(tuple.getObject("stringField", String.class)).contains(source.getStringField());
        assertThat(tuple.getObject("booleanField", Boolean.class)).contains(source.getBooleanField());
        assertThat(tuple.getObject("localDateTimeField", Long.class)).contains(source.getLocalDateTimeField().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        assertThat(tuple.getObject("localDateField", Long.class)).contains(source.getLocalDateField().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        assertThat(tuple.getObject("localTimeField", Long.class)).contains(source.getLocalTimeField().toNanoOfDay());
        assertThat(tuple.getObject("enumField", String.class)).contains(source.getEnumField().name());
        assertThat(tuple.getObject("integerField", Integer.class)).contains(source.getIntegerField());
        assertThat(tuple.getObject("longField", Long.class)).contains(source.getLongField());
        assertThat(tuple.getObject("doubleField", Double.class)).contains(source.getDoubleField());
        assertThat(tuple.getObject("bigDecimalField", BigDecimal.class)).contains(source.getBigDecimalField());
        assertThat(tuple.getObject("uuidField", UUID.class)).contains(source.getUuidField());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldWriteObjectWithCollections() {
        AllPossibleTypes source = AllPossibleTypes.builder()
                .idField("id1")
                .objectField(AllPossibleTypes.builder().idField("id2").stringField("string").build())
                .listOfLongField(List.of(1L, 2L, 3L, 4L, 5L))
                .listOfObjectField(List.of(AllPossibleTypes.builder().idField("id3").build(), AllPossibleTypes.builder().idField("id4").build()))
                .mapOfStringField(Map.of("key1", "value1", "key2", "value2"))
                .mapOfStringAndObjectField(Map.of("id5", AllPossibleTypes.builder().idField("id5").build(), "id6", AllPossibleTypes.builder().idField("id6").build()))
                .build();
        TarantoolTuple tuple = allPossibleTypesEmptyTuple();

        writeConverter.write(source, tuple);
        assertThat(tuple).isNotNull().hasSize(17);
        assertThat(tuple.getObject("idField", String.class)).contains(source.getIdField());
        assertThat(tuple.getObject("stringField", String.class)).isNotPresent();
        assertThat(tuple.getObject("booleanField", Boolean.class)).isNotPresent();
        assertThat(tuple.getObject("localDateTimeField", Long.class)).isNotPresent();
        assertThat(tuple.getObject("localDateField", Long.class)).isNotPresent();
        assertThat(tuple.getObject("localTimeField", Long.class)).isNotPresent();
        assertThat(tuple.getObject("enumField", String.class)).isNotPresent();
        assertThat(tuple.getObject("integerField", Integer.class)).isNotPresent();
        assertThat(tuple.getObject("longField", Long.class)).isNotPresent();
        assertThat(tuple.getObject("doubleField", Double.class)).isNotPresent();
        assertThat(tuple.getObject("bigDecimalField", BigDecimal.class)).isNotPresent();
        assertThat(tuple.getObject("uuidField", UUID.class)).isNotPresent();
        assertThat(tuple.getObject("objectField")).isPresent();
        assertThat(tuple.getObject("listOfLongField")).isPresent();
        assertThat(tuple.getObject("listOfObjectField")).isPresent();
        assertThat(tuple.getObject("mapOfStringField")).isPresent();
        assertThat(tuple.getObject("mapOfStringAndObjectField")).isPresent();

        Map<String, String> objectField = (Map<String, String>) tuple.getObject("objectField").get();
        assertThat(objectField.get("idField")).isEqualTo(source.getObjectField().getIdField());
        assertThat(objectField.get("stringField")).isEqualTo(source.getObjectField().getStringField());

        List<Integer> listField = (List<Integer>) tuple.getObject("listOfLongField").get();
        assertThat(listField).hasSize(source.getListOfLongField().size());
        assertThat(listField.stream().reduce(0, Integer::sum).longValue()).isEqualTo(source.getListOfLongField().stream().reduce(0L, Long::sum));

        List<Map<String, String>> listOfObjectField = (List<Map<String, String>>) tuple.getObject("listOfObjectField").get();
        assertThat(listOfObjectField).hasSize(source.getListOfObjectField().size());
        assertThat(listOfObjectField.get(0).get("idField")).isEqualTo(source.getListOfObjectField().get(0).getIdField());
        assertThat(listOfObjectField.get(1).get("idField")).isEqualTo(source.getListOfObjectField().get(1).getIdField());

        Map<String, String> mapOfStringField = (Map<String, String>) tuple.getObject("mapOfStringField").get();
        assertThat(mapOfStringField).hasSize(source.getMapOfStringField().size());
        assertThat(mapOfStringField.get("key1")).isEqualTo(source.getMapOfStringField().get("key1"));
        assertThat(mapOfStringField.get("key2")).isEqualTo(source.getMapOfStringField().get("key2"));

        Map<String, Map<String, String>> mapOfStringAndObjectField = (Map<String, Map<String, String>>) tuple.getObject("mapOfStringAndObjectField").get();
        assertThat(mapOfStringAndObjectField).hasSize(source.getMapOfStringAndObjectField().size());
        assertThat(mapOfStringAndObjectField.get("id5").get("idField")).isEqualTo(source.getMapOfStringAndObjectField().get("id5").getIdField());
        assertThat(mapOfStringAndObjectField.get("id6").get("idField")).isEqualTo(source.getMapOfStringAndObjectField().get("id6").getIdField());
    }

    @Test
    void shouldNotWriteObjectWithErrorMap() {
        AllPossibleTypes source = AllPossibleTypes.builder()
                .mapOfObjectAndStringField(Map.of(AllPossibleTypes.builder().build(), "test"))
                .build();
        TarantoolTuple tuple = allPossibleTypesEmptyTuple();

        assertThatThrownBy(() -> writeConverter.write(source, tuple))
                .isInstanceOf(MappingException.class)
                .hasMessage("Cannot use a complex object as a key value");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldWriteCollection() {
        List<AllPossibleTypes> sources = List.of(
                AllPossibleTypes.builder().idField("1").build(),
                AllPossibleTypes.builder().idField("2").build(),
                AllPossibleTypes.builder().idField("3").build(),
                AllPossibleTypes.builder().idField("4").build(),
                AllPossibleTypes.builder().idField("5").build()
        );
        ArrayList<Object> tuples = new ArrayList<>();

        writeConverter.write(sources, tuples);
        assertThat(tuples).isNotNull().hasSize(5);
        assertThat(tuples.get(0)).isInstanceOf(Map.class);
        assertThat(((Map<String, Object>) tuples.get(0)).get("idField")).isEqualTo(sources.get(0).getIdField());
    }

    @Test
    void shouldWriteSimpleObjectWithCustomConverter() {
        WithValidConverter source = new WithValidConverter("1");
        TarantoolTuple tuple = withValidConverterEmptyTuple();

        writeConverter.write(source, tuple);
        assertThat(tuple).isNotNull().hasSize(2);
        assertThat(tuple.getObject("id", String.class)).contains(source.getId());
        assertThat(tuple.getObject("_class")).isPresent();
    }

    @Test
    void shouldNotWriteSimpleObjectWithCustomConverter() {
        WithNotValidConverter source = new WithNotValidConverter("1");
        TarantoolTuple tuple = withNotValidConverterEmptyTuple();

        assertThatThrownBy(() -> writeConverter.write(source, tuple))
                .isInstanceOf(MappingException.class)
                .hasMessage("Couldn't convert source object to TarantoolTuple with custom conversions");
    }

    @Test
    void shouldWriteObjectWithCompositePrimaryKey() {
        WithCompositePrimaryKey source = new WithCompositePrimaryKey(new CompositePrimaryKey(UUID.randomUUID(), LocalDate.now()), "test");
        TarantoolTuple tuple = withCompositePrimaryKeyEmptyTuple();

        writeConverter.write(source, tuple);
        assertThat(tuple).isNotNull().hasSize(3);
        assertThat(tuple.getUUID("id")).isEqualTo(source.getKey().getId());
        assertThat(tuple.getLong("date")).isEqualTo(source.getKey().getDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        assertThat(tuple.getString("text")).isEqualTo(source.getText());
    }

    @Test
    void shouldConvertSimpleObject() {
        LocalDateTime now = LocalDateTime.now();
        Object converted = writeConverter.convert(now);
        assertThat(converted).isInstanceOf(Long.class);
        assertThat(converted).isEqualTo(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        converted = writeConverter.convert("test");
        assertThat(converted).isInstanceOf(String.class);
        assertThat(converted).isEqualTo("test");

        converted = writeConverter.convert(1_000_000_000L);
        assertThat(converted).isInstanceOf(Long.class);
        assertThat(converted).isEqualTo(1_000_000_000L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConvertCollection() {
        List<AllPossibleTypes> sources = List.of(
                AllPossibleTypes.builder().idField("1").build(),
                AllPossibleTypes.builder().idField("2").build(),
                AllPossibleTypes.builder().idField("3").build(),
                AllPossibleTypes.builder().idField("4").build(),
                AllPossibleTypes.builder().idField("5").build()
        );

        Object converted = writeConverter.convert(sources);
        assertThat(converted).isInstanceOf(List.class);
        List<?> list = (List<?>) converted;
        assertThat(list).hasSize(5);
        assertThat(list.get(0)).isInstanceOf(Map.class);
        assertThat(((Map<String, Object>) list.get(0)).get("idField")).isEqualTo(sources.get(0).getIdField());
    }
}
