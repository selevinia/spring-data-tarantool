package org.springframework.data.tarantool.core.convert;

import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;

/**
 * Basic Tarantool entity-to-tuple converter interface
 *
 * @author Tatiana Blinova
 */
public interface TarantoolConverter extends EntityConverter<TarantoolPersistentEntity<?>, TarantoolPersistentProperty, Object, Object> {
    /**
     * Return the custom conversions
     *
     * @return the {@link CustomConversions}
     */
    CustomConversions getCustomConversions();

    @Override
    TarantoolMappingContext getMappingContext();

    /**
     * Convert the given object into a value Tarantool will be able to store in space
     *
     * @param source {@link Object} to convert; must not be {@literal null}
     * @return the result of the conversion
     */
    Object convertToWritableType(Object source);
}
