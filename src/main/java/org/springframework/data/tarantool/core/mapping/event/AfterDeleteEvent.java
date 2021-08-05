package org.springframework.data.tarantool.core.mapping.event;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.util.Assert;

/**
 * Event being thrown after a single tuple has been deleted.
 *
 * @author Alexander Rublev
 */
public class AfterDeleteEvent<T> extends TarantoolMappingEvent<TarantoolTuple> {
    private static final long serialVersionUID = 1L;

    private final Class<T> type;

    /**
     * Creates a new {@link AfterSaveEvent}.
     *
     * @param source    must not be {@literal null}.
     * @param type      must not be {@literal null}.
     * @param spaceName must not be {@literal null}.
     */
    public AfterDeleteEvent(TarantoolTuple source, Class<T> type, String spaceName) {
        super(source, spaceName);

        Assert.notNull(type, "Type must not be null!");
        this.type = type;
    }

    /**
     * Returns the type for which the {@link AfterLoadEvent} shall be invoked for.
     *
     * @return type
     */
    public Class<T> getType() {
        return type;
    }
}