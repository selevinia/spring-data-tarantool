package org.springframework.data.tarantool.core;

import io.tarantool.driver.api.TarantoolClientConfig;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.metadata.TarantoolIndexMetadata;
import io.tarantool.driver.api.metadata.TarantoolMetadataOperations;
import io.tarantool.driver.api.metadata.TarantoolSpaceMetadata;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.TarantoolTupleResult;
import io.tarantool.driver.core.metadata.VSpaceToTarantoolSpaceMetadataConverter;
import io.tarantool.driver.core.metadata.TarantoolIndexMetadataConverter;
import io.tarantool.driver.core.tuple.TarantoolTupleImpl;
import io.tarantool.driver.mappers.factories.DefaultMessagePackMapperFactory;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.mappers.TarantoolResultMapper;
import io.tarantool.driver.mappers.factories.ResultMapperFactoryFactoryImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class AbstractTarantoolTemplateTest {

    @Mock
    protected TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;

    @Mock
    protected TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>> spaceOperations;

    @Mock
    protected TarantoolMetadataOperations metadataOperations;

    @Mock
    protected TarantoolConverter customConverter;

    protected final Message messageOne = new Message("1", LocalDateTime.now().minusDays(10).withNano(0), "one");
    protected final Message messageTwo = new Message("2", LocalDateTime.now().withNano(0), "two");
    protected final Message messageThree = new Message("3", LocalDateTime.now().plusMonths(1).withNano(0), "three");

    protected final MessageWithCompositePrimaryKey messageWithCompositePrimaryKey = new MessageWithCompositePrimaryKey(new CompositePrimaryKey("1", LocalDateTime.now().withNano(0)), "composite");
    protected final MessageWithMultiFieldKey messageWithMultiFieldKey = new MessageWithMultiFieldKey("1", LocalDateTime.now().withNano(0), "multi-field");

    protected final MessagePackMapper messagePackMapper = DefaultMessagePackMapperFactory.getInstance().defaultComplexTypesMapper();
    protected final TarantoolClientConfig tarantoolClientConfig = TarantoolClientConfig.builder().withMessagePackMapper(messagePackMapper).build();

    protected Object beforeSaveEntity;
    protected Object beforeConvertEntity;

    protected TarantoolResult<TarantoolTuple> tupleResult(CommonMessage... messages) {
        ResultMapperFactoryFactoryImpl resultMapperFactoryFactory = new ResultMapperFactoryFactoryImpl();
        TarantoolResultMapper<TarantoolTuple> tarantoolResultMapper = resultMapperFactoryFactory.getTarantoolTupleResultMapperFactory().withArrayValueToTarantoolTupleResultConverter(messagePackMapper, spaceMetadata());
        return tarantoolResultMapper.fromValue(tupleArrayValue(messages), TarantoolTupleResult.class);
    }

    protected ArrayValue tupleArrayValue(CommonMessage... messages) {
        Value[] tuples = new Value[messages.length];
        for (int i = 0; i < messages.length; i++) {
            CommonMessage message = messages[i];
            List<Object> values = Arrays.asList(
                    ValueFactory.newString(message.getId()),
                    ValueFactory.newInteger(message.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                    ValueFactory.newString(message.getText())
            );
            tuples[i] = new TarantoolTupleImpl(values, messagePackMapper)
                    .toMessagePackValue(messagePackMapper);
        }
        return ValueFactory.newArray(tuples);
    }

    protected Value tupleValue(CommonMessage message) {
        List<Object> values = Arrays.asList(
                ValueFactory.newString(message.getId()),
                ValueFactory.newInteger(message.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                ValueFactory.newString(message.getText())
        );
        return new TarantoolTupleImpl(values, messagePackMapper).toMessagePackValue(messagePackMapper);
    }

    protected TarantoolSpaceMetadata spaceMetadata() {
        Map<Value, Value> idFieldMap = new HashMap<>();
        idFieldMap.put(ValueFactory.newString("is_nullable"), ValueFactory.newBoolean(false));
        idFieldMap.put(ValueFactory.newString("name"), ValueFactory.newString("id"));
        idFieldMap.put(ValueFactory.newString("type"), ValueFactory.newString("string"));

        Map<Value, Value> dateFieldMap = new HashMap<>();
        dateFieldMap.put(ValueFactory.newString("is_nullable"), ValueFactory.newBoolean(false));
        dateFieldMap.put(ValueFactory.newString("name"), ValueFactory.newString("date"));
        dateFieldMap.put(ValueFactory.newString("type"), ValueFactory.newString("number"));

        Map<Value, Value> textFieldMap = new HashMap<>();
        textFieldMap.put(ValueFactory.newString("is_nullable"), ValueFactory.newBoolean(false));
        textFieldMap.put(ValueFactory.newString("name"), ValueFactory.newString("text"));
        textFieldMap.put(ValueFactory.newString("type"), ValueFactory.newString("string"));

        VSpaceToTarantoolSpaceMetadataConverter converter = VSpaceToTarantoolSpaceMetadataConverter.getInstance();
        return converter.fromValue(ValueFactory.newArray(
                ValueFactory.newInteger(0),         // spaceId
                ValueFactory.newInteger(0),         // ownerId
                ValueFactory.newString("messages"), // spaceName
                ValueFactory.newArray(
                        ValueFactory.newMap(idFieldMap),
                        ValueFactory.newMap(dateFieldMap),
                        ValueFactory.newMap(textFieldMap)
                )
        ));
    }

    protected TarantoolIndexMetadata indexMetadata() {
        TarantoolIndexMetadataConverter converter = new TarantoolIndexMetadataConverter(messagePackMapper);
        return converter.fromValue(ValueFactory.newArray(
                ValueFactory.newInteger(0),        // spaceId
                ValueFactory.newInteger(0),        // indexId
                ValueFactory.newString("primary"), // indexName
                ValueFactory.newString("TREE"),    // indexType
                ValueFactory.newMap(Map.of(ValueFactory.newString("unique"), ValueFactory.newBoolean(true))),
                ValueFactory.newArray()            // fields - not required for this test
        ));
    }

    interface CommonMessage {
        String getId();

        LocalDateTime getDate();

        String getText();
    }

    @Space("messages")
    @Data
    @AllArgsConstructor
    static class Message implements CommonMessage {
        @Id
        private String id;
        private LocalDateTime date;
        private String text;
    }

    @Data
    @AllArgsConstructor
    @PrimaryKeyClass
    static class CompositePrimaryKey {
        @PrimaryKeyField
        private String id;
        @PrimaryKeyField
        private LocalDateTime date;
    }

    @Space("messages")
    @Data
    @AllArgsConstructor
    static class MessageWithCompositePrimaryKey implements CommonMessage {
        @PrimaryKey
        private CompositePrimaryKey key;
        private String text;

        @Override
        public String getId() {
            return key.getId();
        }

        @Override
        public LocalDateTime getDate() {
            return key.getDate();
        }
    }

    @Space("messages")
    @Data
    @AllArgsConstructor
    static class MessageWithMultiFieldKey implements CommonMessage, MapIdentifiable {
        @PrimaryKeyField
        private String id;
        @PrimaryKeyField
        private LocalDateTime date;
        private String text;

        @Override
        public MapId getMapId() {
            return BasicMapId.id("id", id).with("date", date);
        }
    }
}
