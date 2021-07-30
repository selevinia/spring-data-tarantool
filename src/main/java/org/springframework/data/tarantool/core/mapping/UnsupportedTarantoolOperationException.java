package org.springframework.data.tarantool.core.mapping;

import org.springframework.dao.InvalidDataAccessApiUsageException;

public class UnsupportedTarantoolOperationException extends InvalidDataAccessApiUsageException {

	public UnsupportedTarantoolOperationException(String msg) {
		super(msg);
	}

	public UnsupportedTarantoolOperationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
