package org.springframework.data.tarantool.repository.query;

import io.tarantool.driver.api.conditions.Conditions;
import org.reactivestreams.Publisher;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.query.Query;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

/**
 * Reactive PartTree {@link RepositoryQuery} implementation for Tarantool.
 *
 * @author Alexander Rublev
 */
public class ReactivePartTreeTarantoolQuery extends TarantoolRepositoryQuerySupport {
    private final ReactiveTarantoolOperations operations;
    private final TarantoolConverter converter;
    private final PartTree tree;

    /**
     * Create a new {@link ReactivePartTreeTarantoolQuery} from the given {@link TarantoolQueryMethod} and
     * {@link ReactiveTarantoolOperations}.
     *
     * @param queryMethod must not be {@literal null}.
     * @param operations  must not be {@literal null}.
     */
    public ReactivePartTreeTarantoolQuery(TarantoolQueryMethod queryMethod,
                                          ReactiveTarantoolOperations operations) {
        super(queryMethod, operations.getConverter());
        this.operations = operations;
        this.converter = operations.getConverter();
        this.tree = new PartTree(queryMethod.getName(), queryMethod.getResultProcessor().getReturnedType().getDomainType());
    }

    @Nullable
    @Override
    public Object execute(Object[] parameters) {
        final Class<?> type = getQueryMethod().getResultProcessor().getReturnedType().getDomainType();
        ParametersParameterAccessor parameterAccessor = new ParametersParameterAccessor(getQueryMethod().getParameters(), parameters);
        TarantoolQueryCreator queryCreator = new TarantoolQueryCreator(tree, parameterAccessor, converter, operations.isProxyClient());

        try {
            Query query = queryCreator.createQuery();
            return Flux.defer(() -> getExecution().execute(query.getConditions(), type));
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    /**
     * Returns calculated {@link ReactivePartTreeTarantoolQuery.PartTreeTarantoolQueryExecution} based on {@link ReactiveTarantoolOperations}.
     *
     * @return never {@literal null}.
     */
    public PartTreeTarantoolQueryExecution getExecution() {
        if (tree.isCountProjection()) {
            return new PartTreeTarantoolQueryExecution.CountExecution(operations);
        } else if (tree.isExistsProjection()) {
            return new PartTreeTarantoolQueryExecution.ExistsExecution(operations);
        } else if (tree.isDelete()) {
            return new PartTreeTarantoolQueryExecution.DeleteExecution(operations);
        } else if (getQueryMethod().isCollectionQuery()) {
            return new PartTreeTarantoolQueryExecution.CollectionExecution(operations);
        } else {
            return new PartTreeTarantoolQueryExecution.SingleEntityExecution(operations);
        }
    }

    /**
     * Part tree query executions for Tarantool.
     *
     * @author Alexander Rublev
     */
    @FunctionalInterface
    interface PartTreeTarantoolQueryExecution {

        Publisher<? extends Object> execute(Conditions conditions, Class<?> type);

        /**
         * {@link ReactivePartTreeTarantoolQuery.PartTreeTarantoolQueryExecution} for count queries.
         *
         * @author Alexander Rublev
         */
        final class CountExecution implements PartTreeTarantoolQueryExecution {
            private final ReactiveTarantoolOperations operations;

            CountExecution(ReactiveTarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Publisher<? extends Object> execute(Conditions conditions, Class<?> type) {
                return operations.count(conditions, type);
            }
        }

        /**
         * {@link ReactivePartTreeTarantoolQuery.PartTreeTarantoolQueryExecution} for exists queries.
         *
         * @author Alexander Rublev
         */
        final class ExistsExecution implements PartTreeTarantoolQueryExecution {
            private final ReactiveTarantoolOperations operations;

            ExistsExecution(ReactiveTarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Publisher<? extends Object> execute(Conditions conditions, Class<?> type) {
                return operations.selectOne(conditions, type).map(v -> true)
                        .defaultIfEmpty(false);
            }
        }

        /**
         * {@link ReactivePartTreeTarantoolQuery.PartTreeTarantoolQueryExecution} for delete queries.
         *
         * @author Alexander Rublev
         */
        final class DeleteExecution implements PartTreeTarantoolQueryExecution {
            private final ReactiveTarantoolOperations operations;

            DeleteExecution(ReactiveTarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Publisher<? extends Object> execute(Conditions conditions, Class<?> type) {
                return operations.delete(conditions, type).then();
            }
        }

        /**
         * {@link ReactivePartTreeTarantoolQuery.PartTreeTarantoolQueryExecution} for collection returning queries.
         *
         * @author Alexander Rublev
         */
        final class CollectionExecution implements PartTreeTarantoolQueryExecution {
            private final ReactiveTarantoolOperations operations;

            CollectionExecution(ReactiveTarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Publisher<? extends Object> execute(Conditions conditions, Class<?> type) {
                return operations.select(conditions, type);
            }
        }

        /**
         * {@link ReactivePartTreeTarantoolQuery.PartTreeTarantoolQueryExecution} to return a single entity.
         *
         * @author Alexander Rublev
         */
        final class SingleEntityExecution implements PartTreeTarantoolQueryExecution {
            private final ReactiveTarantoolOperations operations;

            SingleEntityExecution(ReactiveTarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Publisher<? extends Object> execute(Conditions conditions, Class<?> type) {
                return operations.selectOne(conditions, type);
            }
        }
    }

}