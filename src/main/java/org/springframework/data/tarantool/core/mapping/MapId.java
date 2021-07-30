package org.springframework.data.tarantool.core.mapping;

import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * Interface that represents the id of a persistent entity, where the keys correspond to the entity's JavaBean
 * properties.
 *
 * @author Tatiana Blinova
 */
public interface MapId extends Map<String, Object> {

    /**
     * Builder method that adds the value for the named property, then returns {@code this}.
     *
     * @param name  The property name containing the value.
     * @param value The property value.
     * @return {@code this}
     */
    MapId with(String name, @Nullable Object value);
}
