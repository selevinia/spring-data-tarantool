package org.springframework.data.tarantool.core.mapping.event;

/**
 * {@link TarantoolMappingEvent} triggered before save of an object.
 *
 * @author Alexander Rublev
 */
public class BeforeSaveEvent<E> extends TarantoolMappingEvent<E> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link BeforeSaveEvent}.
     *
     * @param source    must not be {@literal null}.
     * @param spaceName must not be {@literal null}.
     */
    public BeforeSaveEvent(E source, String spaceName) {
        super(source, spaceName);
    }
}
