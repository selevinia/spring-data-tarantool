package org.springframework.data.tarantool.core.mapping.event;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.util.Assert;

/**
 * Event to be triggered after loading {@link TarantoolTuple}s to be mapped onto a given type.
 *
 * @author Alexander Rublev
 */
public class AfterLoadEvent<T> extends TarantoolMappingEvent<TarantoolTuple> {
    private static final long serialVersionUID = 1L;

    private final Class<T> type;

    /**
     * Creates a new {@link AfterLoadEvent} for the given {@link TarantoolTuple}, type and spaceName.
     *
     * @param source    must not be {@literal null}.
     * @param type      must not be {@literal null}.
     * @param spaceName must not be {@literal null}.
     */
    public AfterLoadEvent(TarantoolTuple source, Class<T> type, String spaceName) {
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
