package org.springframework.data.tarantool.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.PrimaryKeyField;

/**
 * Tarantool repository interface using {@link MapId} to represent Ids.
 * <p>
 * This interface uses {@link MapId} for the id type, allowing you to annotate entity fields or properties with
 * {@link PrimaryKeyField}. Use this interface if you do not require a composite primary key class
 * and want to specify the Id with {@link MapId}.
 *
 * @author Alexander Rublev
 */
@NoRepositoryBean
public interface MapIdTarantoolRepository<T> extends TarantoolRepository<T, MapId> {
}
