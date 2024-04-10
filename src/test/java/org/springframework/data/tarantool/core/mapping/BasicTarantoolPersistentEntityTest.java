package org.springframework.data.tarantool.core.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.IdentifierAccessor;
import org.springframework.data.util.TypeInformation;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class BasicTarantoolPersistentEntityTest {

    @Test
    void shouldInheritSpaceAnnotation() {
        BasicTarantoolPersistentEntity<Notification> entity = new BasicTarantoolPersistentEntity<>(TypeInformation.of(Notification.class));
        assertThat(entity.hasSpaceAnnotation()).isTrue();
        assertThat(entity.getSpaceName()).hasToString("messages");
    }

    @Test
    void shouldHasSpaceNameWithoutSpaceAnnotation() {
        BasicTarantoolPersistentEntity<MessageWithoutSpaceAnnotation> entity = new BasicTarantoolPersistentEntity<>(TypeInformation.of(MessageWithoutSpaceAnnotation.class));
        assertThat(entity.hasSpaceAnnotation()).isFalse();
        assertThat(entity.getSpaceName()).hasToString("messagewithoutspaceannotation");
    }

    @Test
    void shouldInheritPrimaryKeyClassAnnotation() {
        BasicTarantoolPersistentEntity<NotificationId> entity = new BasicTarantoolPersistentEntity<>(TypeInformation.of(NotificationId.class));
        assertThat(entity.isCompositePrimaryKeyClass()).isTrue();
    }

    @Test
    void shouldInheritCompositePrimaryKey() {
        TarantoolMappingContext context = new TarantoolMappingContext();
        BasicTarantoolPersistentEntity<?> entity = context.getRequiredPersistentEntity(Notification.class);
        assertThat(entity.hasCompositePrimaryKey()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRejectAssociationCreation() {
        BasicTarantoolPersistentEntity<Notification> entity = new BasicTarantoolPersistentEntity<>(TypeInformation.of(Notification.class));

        assertThatThrownBy(() -> entity.addAssociation(mock(Association.class)))
                .isInstanceOf(UnsupportedTarantoolOperationException.class);
    }

    @Test
    void shouldGetIdentifierAccessorForIdProperty() {
        MessageId id = new MessageId();
        Message message = new Message(id);

        TarantoolMappingContext context = new TarantoolMappingContext();
        BasicTarantoolPersistentEntity<?> entity = context.getRequiredPersistentEntity(Message.class);
        IdentifierAccessor accessor = entity.getIdentifierAccessor(message);

        assertThat(accessor).isNotNull();
        assertThat(accessor.getIdentifier()).isEqualTo(id);
    }

    @Test
    void shouldGetIdentifierAccessorForPersistableEntity() {
        PersistableMessage message = new PersistableMessage(UUID.randomUUID());

        TarantoolMappingContext context = new TarantoolMappingContext();
        BasicTarantoolPersistentEntity<?> entity = context.getRequiredPersistentEntity(PersistableMessage.class);
        IdentifierAccessor accessor = entity.getIdentifierAccessor(message);

        assertThat(accessor).isNotNull();
        assertThat(accessor.getIdentifier()).isEqualTo(message.getId());
    }

    @Test
    void shouldGetIdentifierAccessorForMapIdProperty() {
        MapIdMessage message = new MapIdMessage(UUID.randomUUID());

        TarantoolMappingContext context = new TarantoolMappingContext();
        BasicTarantoolPersistentEntity<?> entity = context.getRequiredPersistentEntity(MapIdMessage.class);
        IdentifierAccessor accessor = entity.getIdentifierAccessor(message);

        assertThat(accessor).isNotNull();
        assertThat(accessor.getIdentifier()).isInstanceOf(MapId.class);
        MapId mapId = (MapId) accessor.getIdentifier();
        assertThat(mapId).isNotNull();
        assertThat(mapId).hasSize(1);
        assertThat(mapId.get("id")).isEqualTo(message.id);
    }

    @Test
    void shouldGetIdentifierAccessorForMapIdentifiableEntity() {
        MapIdentifiableMessage message = new MapIdentifiableMessage(UUID.randomUUID());

        TarantoolMappingContext context = new TarantoolMappingContext();
        BasicTarantoolPersistentEntity<?> entity = context.getRequiredPersistentEntity(MapIdentifiableMessage.class);
        IdentifierAccessor accessor = entity.getIdentifierAccessor(message);

        assertThat(accessor).isNotNull();
        assertThat(accessor.getIdentifier()).isInstanceOf(MapId.class);
        MapId mapId = (MapId) accessor.getIdentifier();
        assertThat(mapId).isEqualTo(message.getMapId());
    }

    @Test
    void shouldGetIdentifierAccessorForEmptyIdProperty() {
        MessageWithoutId message = new MessageWithoutId();

        TarantoolMappingContext context = new TarantoolMappingContext();
        BasicTarantoolPersistentEntity<?> entity = context.getRequiredPersistentEntity(MessageWithoutId.class);
        IdentifierAccessor accessor = entity.getIdentifierAccessor(message);

        assertThat(accessor).isNotNull();
        assertThat(accessor.getIdentifier()).isInstanceOf(MapId.class);
        MapId mapId = (MapId) accessor.getIdentifier();
        assertThat(mapId).isNotNull();
        assertThat(mapId).hasSize(0);
    }

    @PrimaryKeyClass
    private static class MessageId {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Space("messages")
    private static class Message {
        @PrimaryKey
        private MessageId id;
    }

    private static class NotificationId extends MessageId {
    }

    private static class Notification extends Message {
    }

    private static class MessageWithoutSpaceAnnotation {
    }

    private static class MessageWithoutId {
    }

    @Data
    @AllArgsConstructor
    private static class PersistableMessage implements Persistable<UUID> {
        @PrimaryKey
        private UUID id;

        @Override
        public boolean isNew() {
            return true;
        }
    }

    @Data
    @AllArgsConstructor
    private static class MapIdMessage {
        @PrimaryKeyField
        private UUID id;
    }

    @Data
    @AllArgsConstructor
    private static class MapIdentifiableMessage implements MapIdentifiable {
        @PrimaryKeyField
        private UUID id;

        @Override
        public MapId getMapId() {
            return BasicMapId.id("messageId", id);
        }
    }
}
