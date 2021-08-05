package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.domain.Persistable;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.IdentifierAccessor;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.TargetAwareIdentifierAccessor;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.support.IsNewStrategy;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Tarantool specific {@link BasicPersistentEntity} implementation.
 *
 * @author Alexander Rublev
 */
public class BasicTarantoolPersistentEntity<T> extends BasicPersistentEntity<T, TarantoolPersistentProperty> implements TarantoolPersistentEntity<T> {

    public BasicTarantoolPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

    @Override
    public String getSpaceName() {
        Space annotation = findAnnotation(Space.class);

        if (annotation != null && StringUtils.hasText(annotation.value())) {
            return annotation.value();
        }

        return this.getType().getSimpleName().toLowerCase();
    }

    @Override
    public boolean hasSpaceAnnotation() {
        return isAnnotationPresent(Space.class);
    }

    @Override
    public boolean isCompositePrimaryKeyClass() {
        return isAnnotationPresent(PrimaryKeyClass.class);
    }

    @Override
    public boolean hasCompositePrimaryKey() {
        return hasIdProperty() && getRequiredIdProperty().isCompositePrimaryKey();
    }

    @Override
    public void addAssociation(Association<TarantoolPersistentProperty> association) {
        throw new UnsupportedTarantoolOperationException("Tarantool does not support associations");
    }

    @Override
    public IdentifierAccessor getIdentifierAccessor(Object bean) {
        Assert.notNull(bean, "Target bean must not be null");

        if (hasIdProperty() || Persistable.class.isAssignableFrom(getType())) {
            return super.getIdentifierAccessor(bean);
        }
        return new MapIdIdentifierAccessor(this, bean);
    }

    @Override
    protected IsNewStrategy getFallbackIsNewStrategy() {
        return TarantoolPersistentEntityIsNewStrategy.of(this);
    }

    private static class MapIdIdentifierAccessor extends TargetAwareIdentifierAccessor {

        private final Object object;
        private final TarantoolPersistentEntity<?> entity;
        private final PersistentPropertyAccessor<Object> accessor;

        MapIdIdentifierAccessor(TarantoolPersistentEntity<?> entity, Object object) {
            super(object);
            this.object = object;
            this.entity = entity;
            this.accessor = entity.getPropertyAccessor(object);
        }

        @Override
        public Object getIdentifier() {
            if (object instanceof MapIdentifiable) {
                return ((MapIdentifiable) object).getMapId();
            }

            MapId id = BasicMapId.id();
            for (TarantoolPersistentProperty property : entity) {
                if (property.isPrimaryKeyField()) {
                    id.with(property.getName(), accessor.getProperty(property));
                }
            }
            return id;
        }
    }
}
