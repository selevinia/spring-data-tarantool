package org.springframework.data.tarantool.core;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Class to accumulate methods for exception translations
 *
 * @author Tatiana Blinova
 */
public class ExceptionTranslatorSupport {
    private final TarantoolExceptionTranslator exceptionTranslator;

    public ExceptionTranslatorSupport(TarantoolExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = exceptionTranslator;
    }

    public DataAccessException dataAccessException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            DataAccessException dataAccessException = exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
            if (dataAccessException != null) {
                return dataAccessException;
            }
        }
        return new DataRetrievalFailureException(throwable.getMessage(), throwable);
    }
}
