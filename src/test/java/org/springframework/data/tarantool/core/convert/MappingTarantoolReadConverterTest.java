package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class MappingTarantoolReadConverterTest extends AbstractConverterTest {

    private final TarantoolMappingContext mappingContext = new TarantoolMappingContext();
    private final TypeMapper<TarantoolTuple> tupleTypeMapper = new TarantoolTupleTypeMapper();
    private final TypeMapper<Map<String, Object>> mapTypeMapper = new TarantoolMapTypeMapper();
    private final EntityInstantiators instantiators = new EntityInstantiators();
    private final GenericConversionService conversionService = new GenericConversionService();
    private final CustomConversions customConversions = new TarantoolCustomConversions(List.of(new ValidReadingConverter(), new NotValidReadingConverter()));

    private TarantoolReadConverter readConverter;

    @BeforeEach
    void setUp() {
        DefaultConversionService.addCollectionConverters(conversionService);
        customConversions.registerConvertersIn(conversionService);
        readConverter = new MappingTarantoolReadConverter(mappingContext, tupleTypeMapper, mapTypeMapper, () -> instantiators, () -> customConversions, conversionService);
    }

    @Test
    void shouldNotReadWithIncorrectTarget() {
        assertThatThrownBy(() -> readConverter.read(LocalDateTime.class, LocalDateTime.now()))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("Couldn't read from object of type");
    }

    @Test
    void shouldReadEmptyObject() {
        TarantoolTuple tuple = allPossibleTypesEmptyTuple();

        AllPossibleTypes result = readConverter.read(AllPossibleTypes.class, tuple);
        assertThat(result).isNotNull();
        assertThat(result.getIdField()).isNull();
        assertThat(result.getStringField()).isNull();
        assertThat(result.getTransientStringField()).isNull();
        assertThat(result.getBooleanField()).isNull();
        assertThat(result.getLocalDateTimeField()).isNull();
        assertThat(result.getLocalDateField()).isNull();
        assertThat(result.getLocalTimeField()).isNull();
        assertThat(result.getEnumField()).isNull();
        assertThat(result.getIntegerField()).isNull();
        assertThat(result.getLongField()).isNull();
        assertThat(result.getDoubleField()).isNull();
        assertThat(result.getBigDecimalField()).isNull();
        assertThat(result.getUuidField()).isNull();
        assertThat(result.getObjectField()).isNull();
        assertThat(result.getListOfLongField()).isNull();
        assertThat(result.getListOfObjectField()).isNull();
        assertThat(result.getMapOfStringField()).isNull();
        assertThat(result.getMapOfStringAndObjectField()).isNull();
        assertThat(result.getMapOfObjectAndStringField()).isNull();
    }

    @Test
    void shouldReadObjectFromTuple() {
        TarantoolTuple tuple = allPossibleTypesEmptyTuple();
        tuple.putObject("idField", "id1");
        tuple.putObject("stringField", "string");
        tuple.putObject("booleanField", false);
        tuple.putObject("localDateTimeField", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        tuple.putObject("localDateField", LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        tuple.putObject("localTimeField", LocalTime.MIDNIGHT.toNanoOfDay());
        tuple.putObject("enumField", TestEnum.TWO.name());
        tuple.putObject("integerField", 1);
        tuple.putObject("longField", 1_000_000_000_001L);
        tuple.putObject("doubleField", 1.2);
        tuple.putObject("bigDecimalField", new BigDecimal("1.2"));
        tuple.putObject("uuidField", UUID.randomUUID());

        AllPossibleTypes result = readConverter.read(AllPossibleTypes.class, tuple);
        assertThat(result).isNotNull();
        assertThat(result.getIdField()).isEqualTo(tuple.getString("idField"));
        assertThat(result.getStringField()).isEqualTo(tuple.getString("stringField"));
        assertThat(result.getTransientStringField()).isNull();
        assertThat(result.getBooleanField()).isEqualTo(tuple.getBoolean("booleanField"));
        assertThat(result.getLocalDateTimeField().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).isEqualTo(tuple.getLong("localDateTimeField"));
        assertThat(result.getLocalDateField().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).isEqualTo(tuple.getLong("localDateField"));
        assertThat(result.getLocalTimeField().toNanoOfDay()).isEqualTo(tuple.getLong("localTimeField"));
        assertThat(result.getEnumField().name()).isEqualTo(tuple.getString("enumField"));
        assertThat(result.getIntegerField()).isEqualTo(tuple.getInteger("integerField"));
        assertThat(result.getLongField()).isEqualTo(tuple.getLong("longField"));
        assertThat(result.getDoubleField()).isEqualTo(tuple.getDouble("doubleField"), within(0.01));
        assertThat(result.getBigDecimalField()).isEqualTo(tuple.getDecimal("bigDecimalField"));
        assertThat(result.getUuidField()).isEqualTo(tuple.getUUID("uuidField"));
        assertThat(result.getObjectField()).isNull();
        assertThat(result.getListOfLongField()).isNull();
        assertThat(result.getListOfObjectField()).isNull();
        assertThat(result.getMapOfStringField()).isNull();
        assertThat(result.getMapOfStringAndObjectField()).isNull();
        assertThat(result.getMapOfObjectAndStringField()).isNull();
    }

    @Test
    void shouldReadObjectFromMap() {
        Map<String, Object> source = new HashMap<>();
        source.put("idField", "id1");
        source.put("stringField", "string");
        source.put("booleanField", false);
        source.put("localDateTimeField", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        source.put("localDateField", LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        source.put("localTimeField", LocalTime.MIDNIGHT.toNanoOfDay());
        source.put("enumField", TestEnum.TWO.name());
        source.put("integerField", 1);
        source.put("longField", 1_000_000_000_001L);
        source.put("doubleField", 1.2);
        source.put("bigDecimalField", new BigDecimal("1.2"));
        source.put("uuidField", UUID.randomUUID());

        AllPossibleTypes result = readConverter.read(AllPossibleTypes.class, source);
        assertThat(result).isNotNull();
        assertThat(result.getIdField()).isEqualTo(source.get("idField"));
        assertThat(result.getStringField()).isEqualTo(source.get("stringField"));
        assertThat(result.getTransientStringField()).isNull();
        assertThat(result.getBooleanField()).isEqualTo(source.get("booleanField"));
        assertThat(result.getLocalDateTimeField().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).isEqualTo(source.get("localDateTimeField"));
        assertThat(result.getLocalDateField().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).isEqualTo(source.get("localDateField"));
        assertThat(result.getLocalTimeField().toNanoOfDay()).isEqualTo(source.get("localTimeField"));
        assertThat(result.getEnumField().name()).isEqualTo(source.get("enumField"));
        assertThat(result.getIntegerField()).isEqualTo(source.get("integerField"));
        assertThat(result.getLongField()).isEqualTo(source.get("longField"));
        assertThat(result.getDoubleField()).isEqualTo((Double) source.get("doubleField"), within(0.01));
        assertThat(result.getBigDecimalField()).isEqualTo(source.get("bigDecimalField"));
        assertThat(result.getUuidField()).isEqualTo(source.get("uuidField"));
        assertThat(result.getObjectField()).isNull();
        assertThat(result.getListOfLongField()).isNull();
        assertThat(result.getListOfObjectField()).isNull();
        assertThat(result.getMapOfStringField()).isNull();
        assertThat(result.getMapOfStringAndObjectField()).isNull();
        assertThat(result.getMapOfObjectAndStringField()).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReadObjectWithCollections() {
        TarantoolTuple tuple = allPossibleTypesEmptyTuple();
        tuple.putObject("idField", "id1");
        tuple.putObject("objectField", Map.of("idField", "id2", "stringField", "string"));
        tuple.putObject("listOfLongField", List.of(1, 2, 3, 4, 5));
        tuple.putObject("listOfObjectField", List.of(Map.of("idField", "id3"), Map.of("idField", "id4")));
        tuple.putObject("mapOfStringField", Map.of("key1", "value1", "key2", "value2"));
        tuple.putObject("mapOfStringAndObjectField", Map.of("id5", Map.of("idField", "id5"), "id6", Map.of("idField", "id6")));

        AllPossibleTypes result = readConverter.read(AllPossibleTypes.class, tuple);
        assertThat(result).isNotNull();
        assertThat(result.getIdField()).isEqualTo(tuple.getString("idField"));
        assertThat(result.getStringField()).isNull();
        assertThat(result.getTransientStringField()).isNull();
        assertThat(result.getBooleanField()).isNull();
        assertThat(result.getLocalDateTimeField()).isNull();
        assertThat(result.getLocalDateField()).isNull();
        assertThat(result.getLocalTimeField()).isNull();
        assertThat(result.getEnumField()).isNull();
        assertThat(result.getIntegerField()).isNull();
        assertThat(result.getLongField()).isNull();
        assertThat(result.getDoubleField()).isNull();
        assertThat(result.getBigDecimalField()).isNull();
        assertThat(result.getUuidField()).isNull();
        assertThat(result.getMapOfObjectAndStringField()).isNull();

        assertThat(result.getObjectField()).isNotNull();
        assertThat(result.getObjectField().getIdField()).isEqualTo(tuple.getMap("objectField").get("idField"));
        assertThat(result.getObjectField().getStringField()).isEqualTo(tuple.getMap("objectField").get("stringField"));

        assertThat(result.getListOfLongField()).isNotNull();
        assertThat(result.getListOfLongField()).hasSize(tuple.getList("listOfLongField").size());
        assertThat(result.getListOfLongField().stream().reduce(0L, Long::sum)).isEqualTo(((List<Integer>) tuple.getList("listOfLongField")).stream().reduce(0, Integer::sum).longValue());

        assertThat(result.getListOfObjectField()).isNotNull();
        assertThat(result.getListOfObjectField()).hasSize(tuple.getList("listOfObjectField").size());
        assertThat(result.getListOfObjectField().get(0).getIdField()).isEqualTo(((Map<String, String>) tuple.getList("listOfObjectField").get(0)).get("idField"));
        assertThat(result.getListOfObjectField().get(1).getIdField()).isEqualTo(((Map<String, String>) tuple.getList("listOfObjectField").get(1)).get("idField"));

        assertThat(result.getMapOfStringField()).isNotNull();
        assertThat(result.getMapOfStringField()).hasSize(tuple.getMap("mapOfStringField").size());
        assertThat(result.getMapOfStringField().get("key1")).isEqualTo(tuple.getMap("mapOfStringField").get("key1"));
        assertThat(result.getMapOfStringField().get("key2")).isEqualTo(tuple.getMap("mapOfStringField").get("key2"));

        assertThat(result.getMapOfStringAndObjectField()).isNotNull();
        assertThat(result.getMapOfStringAndObjectField()).hasSize(tuple.getMap("mapOfStringAndObjectField").size());
        assertThat(result.getMapOfStringAndObjectField().get("id5").getIdField()).isEqualTo(((Map<String, String>) tuple.getMap("mapOfStringAndObjectField").get("id5")).get("idField"));
        assertThat(result.getMapOfStringAndObjectField().get("id6").getIdField()).isEqualTo(((Map<String, String>) tuple.getMap("mapOfStringAndObjectField").get("id6")).get("idField"));
    }

    @Test
    void shouldReadSimpleObjectWithCustomConverter() {
        TarantoolTuple tuple = withValidConverterEmptyTuple();
        tuple.putObject("id", "1");

        WithValidConverter result = readConverter.read(WithValidConverter.class, tuple);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
    }

    @Test
    void shouldNotReadSimpleObjectWithCustomConverter() {
        TarantoolTuple tuple = withNotValidConverterEmptyTuple();

        assertThatThrownBy(() -> readConverter.read(WithNotValidConverter.class, tuple))
                .isInstanceOf(MappingException.class)
                .hasMessageStartingWith("Couldn't convert source TarantoolTuple")
                .hasMessageEndingWith("with custom conversions");
    }

    @Test
    void shouldReadObjectWithCompositePrimaryKey() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        String text = "test";

        TarantoolTuple tuple = withCompositePrimaryKeyEmptyTuple();
        tuple.putObject("id", id);
        tuple.putObject("date", date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        tuple.putObject("text", text);

        WithCompositePrimaryKey result = readConverter.read(WithCompositePrimaryKey.class, tuple);
        assertThat(result).isNotNull();
        assertThat(result.getKey().getId()).isEqualTo(id);
        assertThat(result.getKey().getDate()).isEqualTo(date);
        assertThat(result.getText()).isEqualTo(text);
    }
}
