package org.springframework.data.tarantool.core.mapping;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of {@link MapId}.
 *
 * @author Tatiana Blinova
 */
public class BasicMapId implements MapId {

    private final Map<String, Object> map = new HashMap<>();

    /**
     * Create a new and empty {@link BasicMapId}.
     */
    public BasicMapId() {
    }

    /**
     * Create a new {@link BasicMapId} given a {@link Map} of key-value tuples.
     *
     * @param map must not be {@literal null}.
     */
    public BasicMapId(Map<String, Object> map) {
        Assert.notNull(map, "Map must not be null");

        this.map.putAll(map);
    }

    /**
     * Factory method. Convenient if imported statically.
     *
     * @return {@link BasicMapId}
     */
    public static MapId id() {
        return new BasicMapId();
    }

    /**
     * Factory method. Convenient if imported statically.
     *
     * @return {@link BasicMapId}
     */
    public static MapId id(String name, Object value) {
        return new BasicMapId().with(name, value);
    }

    /**
     * Factory method. Convenient if imported statically.
     *
     * @return {@link BasicMapId}
     */
    public static MapId id(MapId id) {
        return new BasicMapId(id);
    }

    @Override
    public BasicMapId with(String name, @Nullable Object value) {
        put(name, value);
        return this;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object name) {
        return map.containsKey(name);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (!(that instanceof Map)) {
            return false;
        }
        return map.equals(that);
    }

    @Override
    public Object get(Object name) {
        return map.get(name);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Object put(String name, Object value) {
        return map.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> source) {
        map.putAll(source);
    }

    @Override
    public Object remove(Object name) {
        return map.remove(name);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{ ");

        boolean first = true;
        for (Entry<String, Object> entry : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }
            s.append(entry.getKey()).append(" : ").append(entry.getValue());
        }

        return s.append(" }").toString();
    }
}
