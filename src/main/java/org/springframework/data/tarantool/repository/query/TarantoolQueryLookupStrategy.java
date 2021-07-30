package org.springframework.data.tarantool.repository.query;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;

import java.lang.reflect.Method;

public class TarantoolQueryLookupStrategy implements QueryLookupStrategy {

    private final ReactiveTarantoolOperations operations;
    private final MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext;

    public TarantoolQueryLookupStrategy(ReactiveTarantoolOperations operations,
                                        MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext) {
        this.operations = operations;
        this.mappingContext = mappingContext;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        TarantoolQueryMethod queryMethod = new TarantoolQueryMethod(method, metadata, factory, mappingContext);
        if (queryMethod.hasAnnotatedQuery()) {
            return new ReactiveDirectTarantoolQuery(queryMethod, operations);
        } else {
            return new ReactivePartTreeTarantoolQuery(queryMethod, operations);
        }
    }
}
