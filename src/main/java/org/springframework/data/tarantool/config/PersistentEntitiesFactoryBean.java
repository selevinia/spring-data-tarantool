package org.springframework.data.tarantool.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;

/**
 * Simple helper to be able to wire the {@link MappingContext} from a {@link TarantoolConverter} bean available in the
 * application context.
 */
public class PersistentEntitiesFactoryBean implements FactoryBean<PersistentEntities> {
    private final TarantoolConverter converter;

    /**
     * Creates a new {@link PersistentEntitiesFactoryBean} for the given {@link TarantoolConverter}.
     *
     * @param converter must not be {@literal null}.
     */
    public PersistentEntitiesFactoryBean(TarantoolConverter converter) {
        this.converter = converter;
    }

    @Override
    public PersistentEntities getObject() {
        return PersistentEntities.of(converter.getMappingContext());
    }

    @Override
    public Class<?> getObjectType() {
        return PersistentEntities.class;
    }

}
