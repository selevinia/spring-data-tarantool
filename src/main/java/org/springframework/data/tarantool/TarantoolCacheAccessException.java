package org.springframework.data.tarantool;

import org.springframework.dao.DataAccessException;

public class TarantoolCacheAccessException extends DataAccessException {

    public TarantoolCacheAccessException(String msg) {
        super(msg);
    }

    public TarantoolCacheAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
