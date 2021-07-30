package org.springframework.data.tarantool;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.lang.Nullable;

public class TarantoolDataRetrievalException extends DataRetrievalFailureException {

    public TarantoolDataRetrievalException(String msg) {
        super(msg);
    }

    public TarantoolDataRetrievalException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
