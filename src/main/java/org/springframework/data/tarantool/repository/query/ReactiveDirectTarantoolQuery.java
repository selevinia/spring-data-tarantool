package org.springframework.data.tarantool.repository.query;

import org.reactivestreams.Publisher;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

/**
 * Base class for reactive {@link RepositoryQuery} implementations for Tarantool.
 *
 * @author Alexander Rublev
 */
public class ReactiveDirectTarantoolQuery extends TarantoolRepositoryQuerySupport {
    private final ReactiveTarantoolOperations operations;

    /**
     * Create a new {@link ReactiveDirectTarantoolQuery} from the given {@link ReactiveTarantoolQueryMethod} and
     * {@link ReactiveTarantoolOperations}.
     *
     * @param queryMethod must not be {@literal null}.
     * @param operations  must not be {@literal null}.
     */
    public ReactiveDirectTarantoolQuery(ReactiveTarantoolQueryMethod queryMethod, ReactiveTarantoolOperations operations) {
        super(queryMethod, operations.getConverter());
        this.operations = operations;
    }

    @Nullable
    @Override
    public Object execute(Object[] parameters) {
        Assert.notNull(getQueryMethod().getAnnotatedQuery(), "Query must not be empty");

        final Class<?> type = resolveResultType(getQueryMethod().getResultProcessor());
        return Flux.defer(() -> getExecution().execute(getQueryMethod().getAnnotatedQuery(), parameters, type));
    }

    /**
     * Returns calculated {@link DirectTarantoolQueryExecution} based on {@link ReactiveTarantoolOperations}.
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

        Publisher<? extends Object> execute(String functionName, final Object[] parameters, Class<?> type);

        /**
         * {@link DirectTarantoolQueryExecution} for collection returning queries.
         *
         * @author Alexander Rublev
         */
        final class CollectionExecution implements DirectTarantoolQueryExecution {
            private final ReactiveTarantoolOperations operations;

            CollectionExecution(ReactiveTarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Publisher<? extends Object> execute(String functionName, final Object[] parameters, Class<?> type) {
                return operations.callForAll(functionName, parameters, type);
            }
        }

        /**
         * {@link DirectTarantoolQueryExecution} to return a single entity.
         *
         * @author Alexander Rublev
         */
        final class SingleEntityExecution implements DirectTarantoolQueryExecution {
            private final ReactiveTarantoolOperations operations;

            SingleEntityExecution(ReactiveTarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Publisher<? extends Object> execute(String functionName, final Object[] parameters, Class<?> type) {
                return operations.call(functionName, parameters, type);
            }
        }
    }

}
