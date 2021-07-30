package org.springframework.data.tarantool;

import org.springframework.dao.InvalidDataAccessApiUsageException;

public class TarantoolDataAccessException extends InvalidDataAccessApiUsageException {

    public TarantoolDataAccessException(String msg) {
        super(msg);
    }

    public TarantoolDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
