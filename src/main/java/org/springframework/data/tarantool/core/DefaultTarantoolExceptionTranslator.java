package org.springframework.data.tarantool.core;

import io.tarantool.driver.exceptions.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.tarantool.*;
import org.springframework.lang.Nullable;

/**
 * Simple {@link PersistenceExceptionTranslator} for Tarantool.
 * <p>
 * Convert the given runtime exception to an appropriate exception from the {@code org.springframework.dao} hierarchy.
 * Preserves exception if it's already a {@link DataAccessException} and ignores non {@link io.tarantool.driver.exceptions.TarantoolException}s returning
 * {@literal null}.
 *
 * @author Alexander Rublev
 */
public class DefaultTarantoolExceptionTranslator implements TarantoolExceptionTranslator {

    @Nullable
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException exception) {
        if (exception instanceof DataAccessException) {
            return (DataAccessException) exception;
        }

        return translate(exception);
    }

    @Override
    public DataAccessException translate(RuntimeException exception) {
        if (exception instanceof NoAvailableConnectionsException) {
            return new TarantoolServerConnectionException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolAttemptsLimitException) {
            return new TarantoolServerConnectionException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolTimeoutException) {
            return new TarantoolServerConnectionException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolConnectionException) {
            return new TarantoolServerConnectionException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolSocketException) {
            return new TarantoolServerConnectionException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolFieldNotFoundException) {
            return new TarantoolDataAccessException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolMetadataRequestException) {
            return new TarantoolDataAccessException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolEmptyMetadataException) {
            return new TarantoolDataAccessException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolSpaceOperationException) {
            return new TarantoolDataAccessException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolTupleConversionException) {
            return new TarantoolDataAccessException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolAccessDeniedException) {
            return new TarantoolDataAccessException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolClientException) {
            return new TarantoolServerConnectionException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolInternalException) {
            return new TarantoolDataRetrievalException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolFunctionCallException) {
            return new TarantoolDataRetrievalException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolIndexNotFoundException) {
            return new TarantoolDataRetrievalException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolNoSuchProcedureException) {
            return new TarantoolDataRetrievalException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolSpaceFieldNotFoundException) {
            return new TarantoolSpaceMetadataException(exception.getMessage(), exception);
        }

        if (exception instanceof TarantoolSpaceNotFoundException) {
            return new TarantoolSpaceMetadataException(exception.getMessage(), exception);
        }

        // unknown or unhandled exception
        return new TarantoolUncategorizedException(exception.getMessage(), exception);
    }
}
