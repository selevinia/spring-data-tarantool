package org.springframework.data.tarantool.repository.query;

import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

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
        Assert.notNull(getQueryMethod().getAnnotatedQuery(), "Query must not be empty");

        final Class<?> type = resolveResultType(getQueryMethod().getResultProcessor());
        return getExecution().execute(getQueryMethod().getAnnotatedQuery(), parameters, type);
    }

    /**
     * Returns calculated {@link DirectTarantoolQueryExecution} based on {@link TarantoolOperations}.
     *
     * @return never {@literal null}.
     */
    public DirectTarantoolQueryExecution getExecution() {
        if (getQueryMethod().isCollectionQuery()) {
            return new DirectTarantoolQueryExecution.CollectionExecution(operations);
        } else {
            return new DirectTarantoolQueryExecution.SingleEntityExecution(operations);
        }
    }

    /**
     * Direct query executions for Tarantool.
     *
     * @author Alexander Rublev
     */
    @FunctionalInterface
    interface DirectTarantoolQueryExecution {

        @Nullable
        Object execute(String functionName, final Object[] parameters, Class<?> type);

        /**
         * {@link DirectTarantoolQueryExecution} for collection returning queries.
         *
         * @author Alexander Rublev
         */
        final class CollectionExecution implements DirectTarantoolQueryExecution {
            private final TarantoolOperations operations;

            CollectionExecution(TarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Object execute(String functionName, final Object[] parameters, Class<?> type) {
                return operations.callForAll(functionName, parameters, type);
            }
        }

        /**
         * {@link DirectTarantoolQueryExecution} to return a single entity.
         *
         * @author Alexander Rublev
         */
        final class SingleEntityExecution implements DirectTarantoolQueryExecution {
            private final TarantoolOperations operations;

            SingleEntityExecution(TarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Object execute(String functionName, final Object[] parameters, Class<?> type) {
                return operations.call(functionName, parameters, type);
            }
        }
    }

}
