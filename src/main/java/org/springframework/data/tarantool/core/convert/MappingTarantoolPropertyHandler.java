package org.springframework.data.tarantool.core.convert;

import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;

public class MappingTarantoolPropertyHandler implements PropertyHandler<TarantoolPersistentProperty> {

    private final TarantoolPersistentEntity<?> entity;
    private final PropertyValueProvider<TarantoolPersistentProperty> provider;
    private final PersistentPropertyAccessor<?> accessor;

    public MappingTarantoolPropertyHandler(TarantoolPersistentEntity<?> entity, PropertyValueProvider<TarantoolPersistentProperty> provider, PersistentPropertyAccessor<?> accessor) {
        this.entity = entity;
        this.provider = provider;
        this.accessor = accessor;
    }

    @Override
    public void doWithPersistentProperty(TarantoolPersistentProperty property) {
        if (entity.isCreatorArgument(property)) {
            return;
        }
        Object propertyValue = provider.getPropertyValue(property);
        if (property.getType().isPrimitive() && propertyValue == null) {
            return;
        }
        accessor.setProperty(property, propertyValue);
    }
}
