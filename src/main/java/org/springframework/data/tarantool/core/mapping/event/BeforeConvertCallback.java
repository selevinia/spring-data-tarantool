package org.springframework.data.tarantool.core.mapping.event;

import org.springframework.data.mapping.callback.EntityCallback;

/**
 * Callback being invoked before a domain object is converted to be persisted.
 *
 * @author Alexander Rublev
 */
@FunctionalInterface
public interface BeforeConvertCallback<T> extends EntityCallback<T> {

	/**
	 * Entity callback method invoked before a domain object is converted to be persisted. Can return either the same or a
	 * modified instance of the domain object.
	 *
	 * @param entity the domain object to save.
	 * @param spaceName name of the space.
	 * @return the domain object to be persisted.
	 */
	T onBeforeConvert(T entity, String spaceName);
}
