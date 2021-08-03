package org.springframework.data.tarantool.repository.query;

import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.lang.Nullable;

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
        //TODO should be implemented
        return null;
    }
}
