package org.springframework.data.tarantool.core.mapping.event;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.util.Assert;

/**
 * Event to be triggered after converting a {@link TarantoolTuple}.
 *
 * @author Alexander Rublev
 */
public class AfterConvertEvent<E> extends TarantoolMappingEvent<E> {
    private static final long serialVersionUID = 1L;

    private final TarantoolTuple tuple;

    /**
     * Creates a new {@link AfterConvertEvent} for the given {@code source} and spaceName.
     *
     * @param tuple     must not be {@literal null}.
     * @param source    must not be {@literal null}.
     * @param spaceName must not be {@literal null}.
     */
    public AfterConvertEvent(TarantoolTuple tuple, E source, String spaceName) {
        super(source, spaceName);

        Assert.notNull(tuple, "TarantoolTuple must not be null");
        this.tuple = tuple;
    }

    /**
     * Returns the {@link TarantoolTuple} from which this {@link AfterConvertEvent} was derived.
     *
     * @return Tarantool tuple
     */
    public TarantoolTuple getTuple() {
        return tuple;
    }
}
