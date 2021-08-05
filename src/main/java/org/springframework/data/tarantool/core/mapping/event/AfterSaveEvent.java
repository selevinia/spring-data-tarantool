package org.springframework.data.tarantool.core.mapping.event;

/**
 * {@link TarantoolMappingEvent} triggered after save of an object.
 *
 * @author Alexander Rublev
 */
public class AfterSaveEvent<E> extends TarantoolMappingEvent<E> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link AfterSaveEvent}.
     *
     * @param source    must not be {@literal null}.
     * @param spaceName must not be {@literal null}.
     */
    public AfterSaveEvent(E source, String spaceName) {
        super(source, spaceName);
    }
}
