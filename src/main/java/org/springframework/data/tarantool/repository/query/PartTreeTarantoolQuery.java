package org.springframework.data.tarantool.repository.query;

import io.tarantool.driver.api.conditions.Conditions;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.query.Query;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * PartTree {@link RepositoryQuery} implementation for Tarantool.
 *
 * @author Alexander Rublev
 */
public class PartTreeTarantoolQuery extends TarantoolRepositoryQuerySupport {
    private final TarantoolOperations operations;
    private final TarantoolConverter converter;
    private final PartTree tree;

    /**
     * Create a new {@link PartTreeTarantoolQuery} from the given {@link ReactiveTarantoolQueryMethod} and
     * {@link TarantoolOperations}.
     *
     * @param queryMethod must not be {@literal null}.
     * @param operations  must not be {@literal null}.
     */
    public PartTreeTarantoolQuery(ReactiveTarantoolQueryMethod queryMethod,
                                  TarantoolOperations operations) {
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
        Query query = queryCreator.createQuery();
        return getExecution().execute(query.getConditions(), type);
    }

    /**
     * Returns calculated {@link PartTreeTarantoolQueryExecution} based on {@link TarantoolOperations}.
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

        Object execute(Conditions conditions, Class<?> type);

        /**
         * {@link PartTreeTarantoolQueryExecution} for count queries.
         *
         * @author Alexander Rublev
         */
        final class CountExecution implements PartTreeTarantoolQueryExecution {
            private final TarantoolOperations operations;

            CountExecution(TarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Object execute(Conditions conditions, Class<?> type) {
                return operations.count(conditions, type);
            }
        }

        /**
         * {@link PartTreeTarantoolQueryExecution} for exists queries.
         *
         * @author Alexander Rublev
         */
        final class ExistsExecution implements PartTreeTarantoolQueryExecution {
            private final TarantoolOperations operations;

            ExistsExecution(TarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Object execute(Conditions conditions, Class<?> type) {
                return Optional.ofNullable(operations.selectOne(conditions, type)).isPresent();
            }
        }

        /**
         * {@link PartTreeTarantoolQueryExecution} for delete queries.
         *
         * @author Alexander Rublev
         */
        final class DeleteExecution implements PartTreeTarantoolQueryExecution {
            private final TarantoolOperations operations;

            DeleteExecution(TarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Object execute(Conditions conditions, Class<?> type) {
                return operations.delete(conditions, type);
            }
        }

        /**
         * {@link PartTreeTarantoolQueryExecution} for collection returning queries.
         *
         * @author Alexander Rublev
         */
        final class CollectionExecution implements PartTreeTarantoolQueryExecution {
            private final TarantoolOperations operations;

            CollectionExecution(TarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            public Object execute(Conditions conditions, Class<?> type) {
                return operations.select(conditions, type);
            }
        }

        /**
         * {@link PartTreeTarantoolQueryExecution} to return a single entity.
         *
         * @author Alexander Rublev
         */
        final class SingleEntityExecution implements PartTreeTarantoolQueryExecution {
            private final TarantoolOperations operations;

            SingleEntityExecution(TarantoolOperations operations) {
                this.operations = operations;
            }

            @Override
            @Nullable
            public Object execute(Conditions conditions, Class<?> type) {
                return operations.selectOne(conditions, type);
            }
        }
    }

}
