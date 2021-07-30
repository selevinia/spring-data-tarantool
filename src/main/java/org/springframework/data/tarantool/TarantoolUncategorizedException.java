package org.springframework.data.tarantool;

import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.lang.Nullable;

public class TarantoolUncategorizedException extends UncategorizedDataAccessException {

    public TarantoolUncategorizedException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
