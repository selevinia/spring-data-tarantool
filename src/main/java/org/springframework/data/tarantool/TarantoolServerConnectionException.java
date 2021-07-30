package org.springframework.data.tarantool;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.lang.Nullable;

public class TarantoolServerConnectionException extends DataAccessResourceFailureException {

    public TarantoolServerConnectionException(String msg) {
        super(msg);
    }

    public TarantoolServerConnectionException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
