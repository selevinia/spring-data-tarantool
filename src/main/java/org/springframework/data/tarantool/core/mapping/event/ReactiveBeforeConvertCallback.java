package org.springframework.data.tarantool.core.mapping.event;

import org.reactivestreams.Publisher;
import org.springframework.data.mapping.callback.EntityCallback;

/**
 * Callback being invoked before a domain object is converted to be persisted.
 *
 * @author Alexander Rublev
 */
@FunctionalInterface
public interface ReactiveBeforeConvertCallback<T> extends EntityCallback<T> {

    /**
     * Entity callback method invoked before a domain object is converted to be persisted. Can return either the same or a
     * modified instance of the domain object.
     *
     * @param entity the domain object to save.
     * @param spaceName the name of the space to save object
     * @return a {@link Publisher} emitting the domain object to be persisted.
     */
    Publisher<T> onBeforeConvert(T entity, String spaceName);
}
