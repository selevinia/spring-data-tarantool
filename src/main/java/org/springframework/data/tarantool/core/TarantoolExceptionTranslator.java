package org.springframework.data.tarantool.core;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.tarantool.core.mapping.UnsupportedTarantoolOperationException;

/**
 * Strategy interface for translating between {@link RuntimeException driver exceptions} and Spring's data access
 * strategy-agnostic {@link DataAccessException} hierarchy.
 *
 * @author Alexander Rublev
 * @see org.springframework.dao.DataAccessException
 */
@FunctionalInterface
public interface TarantoolExceptionTranslator extends PersistenceExceptionTranslator {

    default DataAccessException translate(RuntimeException ex) {
        DataAccessException translated = translateExceptionIfPossible(ex);
        return translated == null ? new UnsupportedTarantoolOperationException("Cannot translate exception", ex)
                : translated;
    }
}
