package org.springframework.data.tarantool.core.mapping.event;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.data.mapping.callback.EntityCallback;

/**
 * Entity callback triggered before save of a row.
 *
 * @author Alexander Rublev
 */
@FunctionalInterface
public interface BeforeSaveCallback<T> extends EntityCallback<T> {

	/**
	 * Entity callback method invoked before a domain object is saved. Can return either the same of a modified instance
	 * of the domain object.
	 *
	 * @param entity the domain object to save.
	 * @param tuple {@link TarantoolTuple} representing the {@code entity} operation.
	 * @param spaceName the name of the space to save object.
	 * @return the domain object to be persisted.
	 */
	T onBeforeSave(T entity, TarantoolTuple tuple, String spaceName);
}
