package org.springframework.data.tarantool;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

public class TarantoolSpaceMetadataException extends InvalidDataAccessResourceUsageException {

    public TarantoolSpaceMetadataException(String msg) {
        super(msg);
    }

    public TarantoolSpaceMetadataException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
