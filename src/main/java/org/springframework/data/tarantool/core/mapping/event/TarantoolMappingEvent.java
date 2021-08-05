package org.springframework.data.tarantool.core.mapping.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

/**
 * Base {@link ApplicationEvent} triggered by Spring Data Tarantool.
 *
 * @author Tatiana Blinova
 */
public class TarantoolMappingEvent<T> extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final String spaceName;

    /**
     * Creates new {@link TarantoolMappingEvent}.
     *
     * @param source    must not be {@literal null}.
     * @param spaceName must not be {@literal null}.
     */
    public TarantoolMappingEvent(T source, String spaceName) {
        super(source);

        Assert.notNull(spaceName, "Space name must not be null!");
        this.spaceName = spaceName;
    }

    /**
     * @return space name that event refers to.
     */
    public String getSpaceName() {
        return spaceName;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public T getSource() {
        return (T) super.getSource();
    }
}
