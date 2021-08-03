package org.springframework.data.tarantool.core.mapping;

/**
 * Interface that entity classes may choose to implement in order to allow a client of the entity to easily get the
 * entity's {@link MapId}.
 *
 * @author Tatiana Blinova
 */
public interface MapIdentifiable {

    /**
     * Gets the identity of this instance.
     * {@link MapId}.
     *
     * @return identity of this instance
     */
    MapId getMapId();
}
