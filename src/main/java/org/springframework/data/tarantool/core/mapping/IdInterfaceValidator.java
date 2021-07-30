package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.mapping.MappingException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tatiana Blinova
 */
class IdInterfaceValidator {

    /**
     * Validates the form of the given id interface candidate type. If the interface violates the following restrictions,
     * then an {@link IdInterfaceExceptions} is thrown containing all of the violations encountered, which can be obtained
     * from {@link IdInterfaceExceptions#getExceptions()} or, as a convenience,
     * {@link IdInterfaceExceptions#getMessages()}.
     * <p/>
     * Id interfaces are intended to have methods representing setters and getters. Getter methods take the form
     * <ul>
     * <li>{@code PropertyType getPropertyName()} or</li>
     * <li>{@code PropertyType propertyName()}.</li>
     * </ul>
     * Setter methods take the form
     * <ul>
     * <li>{@code void|IdType setPropertyName(PropertyType)},</li>
     * <li>{@code void|IdType withPropertyName(PropertyType)}, or</li>
     * <li>{@code void|IdType propertyName(PropertyType)};</li>
     * </ul>
     * setter methods may also declare that they return their id interface type to support method chaining.
     * <p/>
     * Id interfaces
     * <ul>
     * <li>must be an {@code interface}, not a <code>class</code>,</li>
     * <li>may extend {@link MapId} or any other interface,</li>
     * <li>must have setter methods that only take a single argument,</li>
     * <li>must have setter methods that only return {@code void} or the id interface's type,</li>
     * <li>must not define any getter methods with the literal name "get", and</li>
     * <li>must not define any setter methods with the literal names "set" or "with".</li>
     * </ul>
     *
     * @param id The candidate interface type
     */
    static void validate(Class<?> id) {
        List<MappingException> exceptions = new ArrayList<>();

        if (!id.isInterface()) {
            exceptions.add(new IdInterfaceException(id, null, "Id type must be an interface"));
        }

        for (Method m : id.getDeclaredMethods()) {
            Class<?>[] args = m.getParameterTypes();
            String name = m.getName();
            Class<?> returnType = m.getReturnType();

            switch (args.length) {
                case 0: // then getter

                    if (name.startsWith("get") && name.length() == 3) {
                        exceptions.add(new IdInterfaceException(id, m, "Getter method must have a property name following 'get' prefix"));
                    }

                    if (Void.TYPE.isAssignableFrom(returnType) || Void.class.isAssignableFrom(returnType)) {
                        exceptions.add(new IdInterfaceException(id, m, "Getter method must return a value"));
                    }

                    break;
                case 1: // then setter

                    if (name.startsWith("set") && name.length() == 3) {
                        exceptions.add(new IdInterfaceException(id, m, "Setter method must have a property name following 'set' prefix"));
                    }

                    if (name.startsWith("with") && name.length() == 4) {
                        exceptions.add(new IdInterfaceException(id, m, "Setter method must have a property name following 'with' prefix"));
                    }

                    if (!Void.TYPE.isAssignableFrom(returnType) && !Void.class.equals(returnType) && !id.equals(returnType)) {
                        exceptions.add(new IdInterfaceException(id, m, "Setter method not returning void may only return the same type as their id interface"));
                    }
                    break;
                default:
                    exceptions.add(new IdInterfaceException(id, m, String.format("Id interface methods may only take zero parameters for a getter or one parameter for a setter; found %s", args.length)));
            }
        }

        if (!exceptions.isEmpty()) {
            throw new IdInterfaceExceptions(id, exceptions);
        }
    }
}
