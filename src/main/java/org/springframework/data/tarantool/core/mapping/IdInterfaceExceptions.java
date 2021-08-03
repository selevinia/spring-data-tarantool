package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.mapping.MappingException;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Aggregator of multiple violations for convenience when verifying id interfaces. This allows the framework to
 * communicate all errors at once, rather than one at a time.
 *
 * @author Tatiana Blinova
 */
public class IdInterfaceExceptions extends MappingException {

    private final Collection<MappingException> exceptions;
    private final String className;

    /**
     * Create a new {@link IdInterfaceExceptions} for the given {@code idInterfaceClass} and exceptions.
     *
     * @param idInterfaceClass must not be {@literal null}.
     * @param exceptions       must not be {@literal null}.
     */
    public IdInterfaceExceptions(Class<?> idInterfaceClass, Collection<MappingException> exceptions) {
        super(String.format("Mapping Exceptions for %s", idInterfaceClass.getName()));

        Assert.notNull(idInterfaceClass, "Parameter idInterfaceClass must not be null");

        this.exceptions = Collections.unmodifiableCollection(new LinkedList<>(exceptions));
        this.className = idInterfaceClass.getName();

        this.exceptions.forEach(this::addSuppressed);
    }

    /**
     * Returns a list of the {@link IdInterfaceException}s aggregated within.
     *
     * @return list of exceptions
     */
    public Collection<MappingException> getExceptions() {
        return exceptions;
    }

    /**
     * Returns a list of the {@link IdInterfaceException} messages aggregated within.
     *
     * @return list of exception messages
     */
    public Collection<String> getMessages() {
        return exceptions.stream().map(Throwable::getMessage).collect(Collectors.toList());
    }

    /**
     * Returns the number of exceptions aggregated in this exception.
     *
     * @return number of exceptions
     */
    public int getCount() {
        return exceptions.size();
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder(className).append(":\n");
        for (MappingException e : exceptions) {
            builder.append(e.getMessage()).append("\n");
        }
        return builder.toString();
    }

    public String getIdInterfaceName() {
        return className;
    }
}
