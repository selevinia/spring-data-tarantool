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
import org.springframework.data.mapping.callback.ReactiveEntityCallbacks;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.data.tarantool.core.mapping.event.ReactiveBeforeSaveCallback;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReactiveTarantoolTemplateTest extends AbstractTarantoolTemplateTest {

    private ReactiveTarantoolTemplate reactiveTarantoolTemplate;

    @BeforeEach
    void setUp() {
        when(tarantoolClient.getConfig()).thenReturn(tarantoolClientConfig);

        ReactiveEntityCallbacks callbacks = ReactiveEntityCallbacks.create();
        callbacks.addEntityCallback((ReactiveBeforeSaveCallback<Object>) (entity, tuple, spaceName) -> {
            assertThat(tuple).isNotNull();
            assertThat(spaceName).isNotNull();
            beforeSaveEntity = entity;
            return Mono.just(entity);
        });

        callbacks.addEntityCallback((ReactiveBeforeConvertCallback<Object>) (entity, spaceName) -> {
            assertThat(spaceName).isNotNull();
            beforeConvertEntity = entity;
            return Mono.just(entity);
        });

        reactiveTarantoolTemplate = new ReactiveTarantoolTemplate(tarantoolClient);
        reactiveTarantoolTemplate.setEntityCallbacks(callbacks);
    }

    @Test
    void shouldGetDefaultConverter() {
        TarantoolConverter converter = reactiveTarantoolTemplate.getConverter();
        assertThat(converter).isNotNull();
        assertThat(converter).isNotSameAs(customConverter);
        assertThat(converter.getMappingContext()).isNotNull();
    }

    @Test
    void shouldConfigureAndGetCustomConverter() {
        ReactiveTarantoolTemplate template = new ReactiveTarantoolTemplate(tarantoolClient, customConverter, new DefaultTarantoolExceptionTranslator());
        TarantoolConverter converter = template.getConverter();
        assertThat(converter).isNotNull();
        assertThat(converter).isSameAs(customConverter);
    }

    @Test
    void shouldTranslateException() {
        when(tarantoolClient.space(any())).thenThrow(new TarantoolClientException("Test exception"));

        reactiveTarantoolTemplate.selectById("1", Message.class).as(StepVerifier::create)
                .consumeErrorWith(e -> {
                    assertThat(e).isInstanceOf(DataAccessException.class);
                    assertThat(e).hasRootCauseInstanceOf(TarantoolClientException.class);
                }).verify();
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

        reactiveTarantoolTemplate.selectById(message.getId(), Message.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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

        reactiveTarantoolTemplate.selectById(message.getKey(), MessageWithCompositePrimaryKey.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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

        reactiveTarantoolTemplate.selectById(message.getMapId(), MessageWithMultiFieldKey.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(any());
    }

    @Test
    void shouldSelectByIds() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        reactiveTarantoolTemplate.selectByIds(Flux.just("1", "2", "3"), Message.class).as(StepVerifier::create)
                .expectNext(messageOne, messageOne, messageOne)
                .verifyComplete();

        verify(tarantoolClient, times(3)).space(any());
        verify(spaceOperations, times(3)).select(any());
    }

    @Test
    void shouldSelectOne() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        Conditions query = Conditions.any();
        reactiveTarantoolTemplate.selectOne(query, Message.class).as(StepVerifier::create)
                .expectNext(messageOne)
                .verifyComplete();

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(query);
    }

    @Test
    void shouldSelectWithConditions() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        Conditions query = Conditions.any();
        reactiveTarantoolTemplate.select(query, Message.class).as(StepVerifier::create)
                .expectNext(messageOne, messageTwo, messageThree)
                .verifyComplete();

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(query);
    }

    @Test
    void shouldSelectAll() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        reactiveTarantoolTemplate.select(Message.class).as(StepVerifier::create)
                .expectNext(messageOne, messageTwo, messageThree)
                .verifyComplete();

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(any());
    }

    @Test
    void shouldCountWithConditions() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo)));

        Conditions query = Conditions.any();
        reactiveTarantoolTemplate.count(query, Message.class).as(StepVerifier::create)
                .expectNext(2L)
                .verifyComplete();

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).select(query);
    }

    @Test
    void shouldCountAll() {
        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        reactiveTarantoolTemplate.count(Message.class).as(StepVerifier::create)
                .expectNext(3L)
                .verifyComplete();

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

        reactiveTarantoolTemplate.insert(message, Message.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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

        reactiveTarantoolTemplate.replace(message, Message.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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
            return CompletableFuture.completedFuture(tupleResult(message));
        });

        Conditions query = Conditions.any();
        reactiveTarantoolTemplate.update(query, message, Message.class).as(StepVerifier::create)
                .expectNext(message, message, message)
                .verifyComplete();

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
        reactiveTarantoolTemplate.update(query, message, MessageWithCompositePrimaryKey.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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
        reactiveTarantoolTemplate.update(query, message, MessageWithMultiFieldKey.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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

        reactiveTarantoolTemplate.delete(message, Message.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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

        reactiveTarantoolTemplate.delete(message, MessageWithCompositePrimaryKey.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

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

        reactiveTarantoolTemplate.delete(message, MessageWithMultiFieldKey.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).delete(any());
    }

    @Test
    void shouldDeleteWithConditions() {
        Message message = messageOne;

        when(tarantoolClient.space(any())).thenReturn(spaceOperations);
        when(spaceOperations.select(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));
        when(spaceOperations.delete(any())).thenReturn(CompletableFuture.completedFuture(tupleResult(message)));

        Conditions query = Conditions.any();
        reactiveTarantoolTemplate.delete(query, Message.class).as(StepVerifier::create)
                .expectNext(message, message, message)
                .verifyComplete();

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

        reactiveTarantoolTemplate.deleteById(message.getId(), Message.class).as(StepVerifier::create)
                .expectNext(message)
                .verifyComplete();

        verify(tarantoolClient, times(1)).space(any());
        verify(spaceOperations, times(1)).delete(any());
    }

    @Test
    void shouldTruncate() {
        ProxyTarantoolTupleClient client = mock(ProxyTarantoolTupleClient.class);
        when(client.getConfig()).thenReturn(tarantoolClientConfig);
        when(client.callForSingleResult("crud.truncate", List.of("messages"), Boolean.class)).thenReturn(CompletableFuture.completedFuture(true));

        ReactiveTarantoolTemplate template = new ReactiveTarantoolTemplate(client);
        template.truncate(Message.class).as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        verify(client, times(1)).callForSingleResult("crud.truncate", List.of("messages"), Boolean.class);
    }

    @Test
    void shouldNotTruncate() {
        ProxyTarantoolTupleClient client = mock(ProxyTarantoolTupleClient.class);
        when(client.getConfig()).thenReturn(tarantoolClientConfig);
        when(client.callForSingleResult("crud.truncate", List.of("messages"), Boolean.class)).thenReturn(CompletableFuture.completedFuture(false));

        ReactiveTarantoolTemplate template = new ReactiveTarantoolTemplate(client);
        template.truncate(Message.class).as(StepVerifier::create)
                .consumeErrorWith(e -> assertThat(e).isInstanceOf(TarantoolSpaceOperationException.class))
                .verify();

        verify(client, times(1)).callForSingleResult("crud.truncate", List.of("messages"), Boolean.class);
    }

    @Test
    void shouldNotTruncateWithDefaultClient() {
        reactiveTarantoolTemplate.truncate(Message.class).as(StepVerifier::create)
                .consumeErrorWith(e -> assertThat(e).isInstanceOf(UnsupportedOperationException.class))
                .verify();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallWithEntity() {
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);
        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata()));
        when(tarantoolClient.getResultMapperFactoryFactory()).thenReturn(new DefaultResultMapperFactoryFactory());
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(CallResultMapper.class))).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        reactiveTarantoolTemplate.call("testFunction", List.of(1), Message.class).as(StepVerifier::create)
                .expectNext(messageOne)
                .verifyComplete();

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(CallResultMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallWithConverter() {
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(ValueConverter.class))).thenReturn(CompletableFuture.completedFuture(tupleValue(messageOne)));

        reactiveTarantoolTemplate.call("testFunction", List.of(1), value -> messagePackMapper.fromValue(value, List.class)).as(StepVerifier::create)
                .expectNext(List.of(messageOne.getId(), messageOne.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageOne.getText()))
                .verifyComplete();

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(ValueConverter.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallForAllWithEntity() {
        when(tarantoolClient.metadata()).thenReturn(metadataOperations);

        when(metadataOperations.getSpaceByName(any())).thenReturn(Optional.of(spaceMetadata()));
        when(tarantoolClient.getResultMapperFactoryFactory()).thenReturn(new DefaultResultMapperFactoryFactory());
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(CallResultMapper.class))).thenReturn(CompletableFuture.completedFuture(tupleResult(messageOne, messageTwo, messageThree)));

        reactiveTarantoolTemplate.callForAll("testFunction", List.of(1, 2, 3), Message.class).as(StepVerifier::create)
                .expectNext(messageOne, messageTwo, messageThree)
                .verifyComplete();

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(CallResultMapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallForAllWithConverter() {
        when(tarantoolClient.callForSingleResult(any(), any(), any(), any(ValueConverter.class))).thenReturn(CompletableFuture.completedFuture(tupleArrayValue(messageOne, messageTwo, messageThree).list()));

        reactiveTarantoolTemplate.callForAll("testFunction", List.of(1, 2, 3), value -> messagePackMapper.fromValue(value, List.class)).as(StepVerifier::create)
                .expectNext(List.of(messageOne.getId(), messageOne.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageOne.getText()))
                .expectNext(List.of(messageTwo.getId(), messageTwo.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageTwo.getText()))
                .expectNext(List.of(messageThree.getId(), messageThree.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), messageThree.getText()))
                .verifyComplete();

        verify(tarantoolClient, times(1)).callForSingleResult(any(), any(), any(), any(ValueConverter.class));
    }

}
