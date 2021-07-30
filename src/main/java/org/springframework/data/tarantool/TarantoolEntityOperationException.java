package org.springframework.data.tarantool;

/**
 * Tarantool entity operation exception class.
 *
 * @author Alexander Rublev
 */
public class TarantoolEntityOperationException extends RuntimeException {

    public TarantoolEntityOperationException(Class<?> entityCls, String details, Throwable cause) {
        super(String.format("Operation failed for class %s: %s", entityCls.getTypeName(), details), cause);
    }

    public TarantoolEntityOperationException(String entityName, String details, Throwable cause) {
        super(String.format("Operation failed for entity %s: %s", entityName, details), cause);
    }
}
