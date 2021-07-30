package org.springframework.data.tarantool.core;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Implementation of extended representation of information about an entity.
 *
 * @author Alexander Rublev
 */
public class AdaptableMappedEntity<T> extends MappedEntity<T> implements AdaptableEntity<T> {

    public static <T> AdaptableMappedEntity<T> of(T bean,
                                                  MappingContext<? extends TarantoolPersistentEntity<?>, TarantoolPersistentProperty> mappingContext,
                                                  ConversionService conversionService) {
        TarantoolPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(bean.getClass());
        Assert.notNull(persistentEntity, "TarantoolPersistentEntity for bean must not be null");

        return new AdaptableMappedEntity<>(persistentEntity,
                new ConvertingPropertyAccessor<>(persistentEntity.getPropertyAccessor(bean), conversionService));
    }

    public AdaptableMappedEntity(TarantoolPersistentEntity<?> entity, PersistentPropertyAccessor<T> propertyAccessor) {
        super(entity, propertyAccessor);
    }

    @Override
    public T initializeVersionProperty() {
        if (entity.hasVersionProperty()) {
            TarantoolPersistentProperty versionProperty = entity.getRequiredVersionProperty();
            propertyAccessor.setProperty(versionProperty, versionProperty.getType().isPrimitive() ? 1 : 0);
        }

        return propertyAccessor.getBean();
    }

    @Override
    public T incrementVersion() {
        TarantoolPersistentProperty versionProperty = this.entity.getRequiredVersionProperty();

        Number version = getVersion();
        Number nextVersion = version == null ? 0 : version.longValue() + 1;

        propertyAccessor.setProperty(versionProperty, nextVersion);
        return propertyAccessor.getBean();
    }

    @Override
    @Nullable
    public Number getVersion() {
        TarantoolPersistentProperty versionProperty = this.entity.getRequiredVersionProperty();
        return (Number) propertyAccessor.getProperty(versionProperty);
    }

}
