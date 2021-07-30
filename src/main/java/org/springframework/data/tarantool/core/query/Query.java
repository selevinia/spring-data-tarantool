package org.springframework.data.tarantool.core.query;

import io.tarantool.driver.api.conditions.Conditions;

/**
 * Tarantool query conditions wrapper
 *
 * @author Alexander Rublev
 */
public class Query {
    private final Conditions conditions;

    /**
     * Create new empty query
     */
    public Query() {
        conditions = Conditions.any();
    }

    /**
     * Create new query with limited conditions
     */
    public Query(int limit) {
        conditions = Conditions.limit(limit);
    }

    /**
     * Get built query conditions
     *
     * @return query conditions
     */
    public Conditions getConditions() {
        return conditions;
    }
}
