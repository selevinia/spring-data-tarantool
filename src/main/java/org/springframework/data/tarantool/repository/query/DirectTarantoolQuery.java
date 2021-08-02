package org.springframework.data.tarantool.repository.query;

import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.lang.Nullable;

/**
 * Base class for {@link RepositoryQuery} implementations for Tarantool.
 *
 * @author Alexander Rublev
 */
public class DirectTarantoolQuery extends TarantoolRepositoryQuerySupport {
    private final TarantoolOperations operations;

    /**
     * Create a new {@link DirectTarantoolQuery} from the given {@link TarantoolQueryMethod} and
     * {@link TarantoolOperations}.
     *
     * @param queryMethod must not be {@literal null}.
     * @param operations  must not be {@literal null}.
     */
    public DirectTarantoolQuery(TarantoolQueryMethod queryMethod, TarantoolOperations operations) {
        super(queryMethod, operations.getConverter());
        this.operations = operations;
    }

    @Nullable
    @Override
    public Object execute(Object[] parameters) {
        //TODO should be implemented
        return null;
    }
}
