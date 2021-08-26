package org.springframework.data.tarantool;

public class TarantoolCacheAccessException extends RuntimeException {

    public TarantoolCacheAccessException(String msg) {
        super(msg);
    }

    public TarantoolCacheAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
