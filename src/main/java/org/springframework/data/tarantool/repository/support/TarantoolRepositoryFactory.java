package org.springframework.data.tarantool.repository.support;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.tarantool.repository.TarantoolRepository;
import org.springframework.data.tarantool.repository.query.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Factory to create {@link TarantoolRepository} instances.
 *
 * @author Alexander Rublev
 */
public class TarantoolRepositoryFactory extends RepositoryFactorySupport {
    private final TarantoolOperations operations;
    private final MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext;

    /**
     * Create a new {@link TarantoolRepositoryFactory} with the given {@link TarantoolOperations}.
     *
     * @param operations must not be {@literal null}.
     */
    public TarantoolRepositoryFactory(TarantoolOperations operations) {
        Assert.notNull(operations, "ReactiveTarantoolOperations must not be null");

        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();

        setEvaluationContextProvider(QueryMethodEvaluationContextProvider.DEFAULT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, ID> TarantoolEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        TarantoolPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
        return new MappingTarantoolEntityInformation<>((TarantoolPersistentEntity<T>) entity);
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        TarantoolEntityInformation<?, Object> entityInformation = getEntityInformation(information.getDomainType());
        return getTargetRepositoryViaReflection(information, entityInformation, operations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleTarantoolRepository.class;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new TarantoolQueryLookupStrategy(operations, mappingContext));
    }

    private static class TarantoolQueryLookupStrategy implements QueryLookupStrategy {

        private final TarantoolOperations operations;
        private final MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext;

        public TarantoolQueryLookupStrategy(TarantoolOperations operations,
                                            MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext) {
            this.operations = operations;
            this.mappingContext = mappingContext;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
            TarantoolQueryMethod queryMethod = new TarantoolQueryMethod(method, metadata, factory, mappingContext);
            if (queryMethod.hasAnnotatedQuery()) {
                return new DirectTarantoolQuery(queryMethod, operations);
            } else {
                return new PartTreeTarantoolQuery(queryMethod, operations);
            }
        }
    }

}
