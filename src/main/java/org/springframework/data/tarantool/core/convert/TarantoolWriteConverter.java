package org.springframework.data.tarantool.core.convert;

import org.springframework.data.convert.EntityWriter;

public interface TarantoolWriteConverter extends EntityWriter<Object, Object> {

    Object convert(Object source);
}
