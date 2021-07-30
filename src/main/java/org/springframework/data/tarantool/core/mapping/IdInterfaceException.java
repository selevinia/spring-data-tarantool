package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.mapping.MappingException;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

/**
 * Exception thrown on incorrect mapping of an Id interface.
 *
 * @author Tatiana Blinova
 */
public class IdInterfaceException extends MappingException {

    private final String idInterfaceName;

    private final @Nullable
    String method;

    public IdInterfaceException(Class<?> idInterface, @Nullable Method method, String message) {
        this(idInterface.getName(), method == null ? null : method.toString(), message);
    }

    public IdInterfaceException(String idInterfaceName, @Nullable String method, String message) {
        super(message);
        this.idInterfaceName = idInterfaceName;
        this.method = method;
    }

    public String getIdInterfaceName() {
        return idInterfaceName;
    }

    @Nullable
    public String getMethod() {
        return method;
    }
}
