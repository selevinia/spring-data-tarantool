package org.springframework.data.tarantool.core;

import io.tarantool.driver.ProxyTarantoolTupleClient;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.tuple.operations.TupleOperations;
import io.tarantool.driver.exceptions.TarantoolClientException;
import io.tarantool.driver.exceptions.TarantoolSpaceOperationException;
import io.tarantool.driver.mappers.CallResultMapper;
import io.tarantool.driver.mappers.DefaultResultMapperFactoryFactory;
import io.tarantool.driver.mappers.ValueConverter;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import io.tarantool.driver.protocol.TarantoolIndexQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.tarantool.core.mapping.event.BeforeSaveCallback;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldSelectById() {
        Message message = messageOne;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(1);
            assertThat(indexQuery.getKeyValues().get(0)).isEqualTo(message.getId());
            return (CompletableFuture.completedFuture(tupleResult(message)));
        });

        Message selected = tarantoolTemplate.selectById(message.getId(), Message.class);
        assertThat(selected).isEqualTo(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(any());
    }

    @Test
    void shouldSelectByIdWithCompositePrimaryKey() {
        MessageWithCompositePrimaryKey message = messageWithCompositePrimaryKey;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(2);
            assertThat(indexQuery.getKeyValues().get(0)).isEqualTo(message.getKey().getId());
            assertThat(indexQuery.getKeyValues().get(1)).isEqualTo(message.getKey().getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            return (CompletableFuture.completedFuture(tupleResult(message)));
        });

        MessageWithCompositePrimaryKey selected = tarantoolTemplate.selectById(message.getKey(), MessageWithCompositePrimaryKey.class);
        assertThat(selected).isEqualTo(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(any());
    }

    @Test
    void shouldSelectByIdWithMultiFieldKey() {
        MessageWithMultiFieldKey message = messageWithMultiFieldKey;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(2);
            assertThat(indexQuery.getKeyValues().get(0)).isEqualTo(message.getId());
            assertThat(indexQuery.getKeyValues().get(1)).isEqualTo(message.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            return (CompletableFuture.completedFuture(tupleResult(message)));
        });

        MessageWithMultiFieldKey selected = tarantoolTemplate.selectById(message.getMapId(), MessageWithMultiFieldKey.class);
        assertThat(selected).isEqualTo(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(any());
    }

    @Test
    void shouldSelectByIds() {
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            if (indexQuery.getKeyValues().get(0).equals("1")) {
                return (CompletableFuture.completedFuture(tupleResult(messageOne)));
            } else if (indexQuery.getKeyValues().get(0).equals("2")) {
                return (CompletableFuture.completedFuture(tupleResult(messageTwo)));
            } else {
                return (CompletableFuture.completedFuture(tupleResult(messageThree)));
            }
        });

        List<Message> selected = tarantoolTemplate.selectByIds(List.of("1", "2", "3"), Message.class);
        assertThat(selected).contains(messageOne);
        assertThat(selected).contains(messageTwo);
        assertThat(selected).contains(messageThree);

        verify(tarantoolClient, times(3)).space(any());
        verify(spaceOperations, times(3)).select(any());
    }

    @Test
    void shouldSelectOne() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        Conditions query = Conditions.any();
        Message selected = tarantoolTemplate.selectOne(query, Message.class);
        assertThat(selected).isEqualTo(messageOne);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(query);
    }

    @Test
    void shouldSelectWithConditions() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        Conditions query = Conditions.any();
        List<Message> selected = tarantoolTemplate.select(query, Message.class);
        assertThat(selected).contains(messageOne);
        assertThat(selected).contains(messageTwo);
        assertThat(selected).contains(messageThree);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(query);
    }

    @Test
    void shouldSelectAll() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        List<Message> selected = tarantoolTemplate.select(Message.class);
        assertThat(selected).contains(messageOne);
        assertThat(selected).contains(messageTwo);
        assertThat(selected).contains(messageThree);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(any());
    }

    @Test
    void shouldCountWithConditions() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo)));

        Conditions query = Conditions.any();
        Long count = tarantoolTemplate.count(query, Message.class);
        assertThat(count).isEqualTo(2L);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(query);
    }

    @Test
    void shouldCountAll() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        Long count = tarantoolTemplate.count(Message.class);
        assertThat(count).isEqualTo(3L);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(any());
    }

    @Test
    void shouldInsert() {
        Message message = messageOne;

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);
        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata()));
        when(spaceOperations.insert(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(message)));

        Message inserted = tarantoolTemplate.insert(message, Message.class);
        assertThat(inserted).isEqualTo(message);
        assertThat(beforeConvertEntity).isSameAs(message);
        assertThat(beforeSaveEntity).isSameAs(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(metadataOperations, times(1)).getSpaceByName(any());
        verify(spaceOperations, times(1)).insert(any());
    }

    @Test
    void shouldReplace() {
        Message message = messageTwo;

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);
        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata()));
        when(spaceOperations.replace(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(message)));

        Message replaced = tarantoolTemplate.replace(message, Message.class);
        assertThat(replaced).isEqualTo(message);
        assertThat(beforeConvertEntity).isSameAs(message);
        assertThat(beforeSaveEntity).isSameAs(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(metadataOperations, times(1)).getSpaceByName(any());
        verify(spaceOperations, times(1)).replace(any());
    }

    @Test
    void shouldUpdate() {
        Message message = messageThree;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);
        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata));
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));
        when(spaceOperations.update(any(), any(TupleOperations.class))).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(1);
            if (indexQuery.getKeyValues().get(0).equals("1")) {
                return (CompletableFuture.completedFuture(tupleResult(messageOne)));
            } else if (indexQuery.getKeyValues().get(0).equals("2")) {
                return (CompletableFuture.completedFuture(tupleResult(messageTwo)));
            } else {
                return (CompletableFuture.completedFuture(tupleResult(messageThree)));
            }
        });

        Conditions query = Conditions.any();
        List<Message> updated = tarantoolTemplate.update(query, message, Message.class);
        assertThat(updated).hasSize(3);
        assertThat(updated).contains(messageOne);
        assertThat(updated).contains(messageTwo);
        assertThat(updated).contains(messageThree);

        assertThat(beforeConvertEntity).isSameAs(message);
        assertThat(beforeSaveEntity).isSameAs(message);

        verify(tarantoolClient, times(4)).space(any());
        verify(metadataOperations, times(1)).getSpaceByName(any());
        verify(spaceOperations, times(1)).select(query);
        verify(spaceOperations, times(3)).update(any(), any(TupleOperations.class));
    }

    @Test
    void shouldUpdateWithCompositePrimaryKey() {
        MessageWithCompositePrimaryKey message = messageWithCompositePrimaryKey;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);
        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata));
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(message)));
        when(spaceOperations.update(any(), any(TupleOperations.class))).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(2);
            return CompletableFuture.completedFuture(tupleResult(message));
        });

        Conditions query = Conditions.any();
        List<MessageWithCompositePrimaryKey> updated = tarantoolTemplate.update(query, message, MessageWithCompositePrimaryKey.class);
        assertThat(updated).hasSize(1);

        assertThat(beforeConvertEntity).isSameAs(message);
        assertThat(beforeSaveEntity).isSameAs(message);

        verify(tarantoolClient, times(2)).space(any());
        verify(metadataOperations, times(1)).getSpaceByName(any());
        verify(spaceOperations, times(1)).select(query);
        verify(spaceOperations, times(1)).update(any(), any(TupleOperations.class));
    }

    @Test
    void shouldUpdateWithMultiFieldKey() {
        MessageWithMultiFieldKey message = messageWithMultiFieldKey;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);
        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata));
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(message)));
        when(spaceOperations.update(any(), any(TupleOperations.class))).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(2);
            return CompletableFuture.completedFuture(tupleResult(message));
        });

        Conditions query = Conditions.any();
        List<MessageWithMultiFieldKey> updated = tarantoolTemplate.update(query, message, MessageWithMultiFieldKey.class);
        assertThat(updated).hasSize(1);

        assertThat(beforeConvertEntity).isSameAs(message);
        assertThat(beforeSaveEntity).isSameAs(message);

        verify(tarantoolClient, times(2)).space(any());
        verify(metadataOperations, times(1)).getSpaceByName(any());
        verify(spaceOperations, times(1)).select(query);
        verify(spaceOperations, times(1)).update(any(), any(TupleOperations.class));
    }

    @Test
    void shouldDeleteEntity() {
        Message message = messageOne;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.delete(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(1);
            assertThat(indexQuery.getKeyValues().get(0)).isEqualTo(message.getId());
            return (CompletableFuture.completedFuture(tupleResult(message)));
        });

        Message deleted = tarantoolTemplate.delete(message, Message.class);
        assertThat(deleted).isEqualTo(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).delete(any());
    }

    @Test
    void shouldDeleteEntityWithCompositePrimaryKey() {
        MessageWithCompositePrimaryKey message = messageWithCompositePrimaryKey;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.delete(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(2);
            assertThat(indexQuery.getKeyValues().get(0)).isEqualTo(message.getKey().getId());
            assertThat(indexQuery.getKeyValues().get(1)).isEqualTo(message.getKey().getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            return CompletableFuture.completedFuture(tupleResult(message));
        });

        MessageWithCompositePrimaryKey deleted = tarantoolTemplate.delete(message, MessageWithCompositePrimaryKey.class);
        assertThat(deleted).isEqualTo(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).delete(any());
    }

    @Test
    void shouldDeleteEntityWithMultiFieldKey() {
        MessageWithMultiFieldKey message = messageWithMultiFieldKey;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.delete(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(2);
            assertThat(indexQuery.getKeyValues().get(0)).isEqualTo(message.getId());
            assertThat(indexQuery.getKeyValues().get(1)).isEqualTo(message.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            return CompletableFuture.completedFuture(tupleResult(message));
        });

        MessageWithMultiFieldKey deleted = tarantoolTemplate.delete(message, MessageWithMultiFieldKey.class);
        assertThat(deleted).isEqualTo(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).delete(any());
    }

    @Test
    void shouldDeleteWithConditions() {
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));
        when(spaceOperations.delete(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(1);
            if (indexQuery.getKeyValues().get(0).equals("1")) {
                return (CompletableFuture.completedFuture(tupleResult(messageOne)));
            } else if (indexQuery.getKeyValues().get(0).equals("2")) {
                return (CompletableFuture.completedFuture(tupleResult(messageTwo)));
            } else {
                return (CompletableFuture.completedFuture(tupleResult(messageThree)));
            }
        });

        Conditions query = Conditions.any();
        List<Message> deleted = tarantoolTemplate.delete(query, Message.class);
        assertThat(deleted).hasSize(3);
        assertThat(deleted).contains(messageOne);
        assertThat(deleted).contains(messageTwo);
        assertThat(deleted).contains(messageThree);

        verify(tarantoolClient, times(4)).space(any());
        verify(spaceOperations, times(1)).select(query);
        verify(spaceOperations, times(3)).delete(any());
    }

    @Test
    void shouldDeleteById() {
        Message message = messageOne;
        TarantoolSpaceMetadata spaceMetadata = spaceMetadata();

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(metadataOperations.getIndexById(spaceMetadata.getSpaceName(), 0)).thenReturn(Optional.of(indexMetadata()));
        when(spaceOperations.delete(any())).then(invocation -> {
            Conditions conditions = invocation.getArgument(0);
            TarantoolIndexQuery indexQuery = conditions.toIndexQuery(metadataOperations, spaceMetadata);
            assertThat(indexQuery.getIndexId()).isEqualTo(0);
            assertThat(indexQuery.getKeyValues()).hasSize(1);
            assertThat(indexQuery.getKeyValues().get(0)).isEqualTo(message.getId());
            return (CompletableFuture.completedFuture(tupleResult(message)));
        });

        Message deleted = tarantoolTemplate.deleteById(message.getId(), Message.class);
        assertThat(deleted).isEqualTo(message);

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).delete(any());
    }

    @Test
    void shouldTruncate() {
        ProxyTarantoolTupleClient client = mock(ProxyTarantoolTupleClient.class);
        when(client.getConfig()).thenReturn(tarantoolClientConfig);
        when(client.callForSingleResult("crud.truncate", List.of("messages"), Boolean.class)).thenReturn(CompletableFuture.completedFuture(true));

        TarantoolTemplate template = new TarantoolTemplate(client);
        boolean truncated = template.truncate(Message.class);
        assertThat(truncated).isTrue();

        verify(client, times(1)).callForSingleResult("crud.truncate", List.of("messages"), Boolean.class);
    }

    @Test
    void shouldNotTruncate() {
        ProxyTarantoolTupleClient client = mock(ProxyTarantoolTupleClient.class);
        when(client.getConfig()).thenReturn(tarantoolClientConfig);
        when(client.callForSingleResult("crud.truncate", List.of("messages"), Boolean.class)).thenReturn(CompletableFuture.completedFuture(false));

        TarantoolTemplate template = new TarantoolTemplate(client);
        assertThatThrownBy(() -> template.truncate(Message.class))
                .isInstanceOf(TarantoolSpaceOperationException.class);

        verify(client, times(1)).callForSingleResult("crud.truncate", List.of("messages"), Boolean.class);
    }

    @Test
    void shouldNotTruncateWithDefaultClient() {
        assertThatThrownBy(() -> tarantoolTemplate.truncate(Message.class))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallWithEntity() {
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);
        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata()));
        when(tarantoolClient.getResultMapperFactoryFactory()).thenReturn(new DefaultResultMapperFactoryFactory());
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(CallResultMapper.class))).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        Message received = tarantoolTemplate.call("testFunction", List.of(1), Message.class);
        assertThat(received).isEqualTo(messageOne);

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(CallResultMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallWithConverter() {
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(ValueConverter.class))).thenReturn(CompletableFuture.completedFuture(tupleValue(messageOne)));

        List<Object> received = tarantoolTemplate.call("testFunction", List.of(1), value -> messagePackMapper.fromValue(value, List.class));
        assertThat(received).containsAll(List.of(messageOne.getId(), messageOne.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageOne.getText()));

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(ValueConverter.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallForAllWithEntity() {
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);

        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata()));
        when(tarantoolClient.getResultMapperFactoryFactory()).thenReturn(new DefaultResultMapperFactoryFactory());
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(CallResultMapper.class))).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        List<Message> received = tarantoolTemplate.callForAll("testFunction", List.of(1, 2, 3), Message.class);
        assertThat(received).containsExactly(messageOne, messageTwo, messageThree);

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(CallResultMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallForAllWithConverter() {
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(ValueConverter.class))).thenReturn(CompletableFuture.completedFuture(tupleArrayValue(messageOne, messageTwo, messageThree).list()));

        List<List<Object>> received = tarantoolTemplate.callForAll("testFunction", List.of(1, 2, 3), value -> messagePackMapper.fromValue(value, List.class));
        assertThat(received).containsExactly(
                List.of(messageOne.getId(), messageOne.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageOne.getText()),
                List.of(messageTwo.getId(), messageTwo.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageTwo.getText()),
                List.of(messageThree.getId(), messageThree.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageThree.getText())
        );

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(ValueConverter.class));
    }

}
