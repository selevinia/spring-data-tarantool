package org.springframework.data.tarantool.repository.support;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.ReactiveRepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.tarantool.repository.ReactiveTarantoolRepository;
import org.springframework.data.tarantool.repository.query.ReactiveDirectTarantoolQuery;
import org.springframework.data.tarantool.repository.query.ReactivePartTreeTarantoolQuery;
import org.springframework.data.tarantool.repository.query.ReactiveTarantoolQueryMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Factory to create {@link ReactiveTarantoolRepository} instances.
 *
 * @author Alexander Rublev
 */
public class ReactiveTarantoolRepositoryFactory extends ReactiveRepositoryFactorySupport {
    private final ReactiveTarantoolOperations operations;
    private final MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext;

    /**
     * Create a new {@link ReactiveTarantoolRepositoryFactory} with the given {@link ReactiveTarantoolOperations}.
     *
     * @param operations must not be {@literal null}.
     */
    public ReactiveTarantoolRepositoryFactory(ReactiveTarantoolOperations operations) {
        Assert.notNull(operations, "ReactiveTarantoolOperations must not be null");

        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();

        setEvaluationContextProvider(ReactiveQueryMethodEvaluationContextProvider.DEFAULT);
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
        return SimpleReactiveTarantoolRepository.class;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new TarantoolQueryLookupStrategy(operations, mappingContext));
    }

    private static class TarantoolQueryLookupStrategy implements QueryLookupStrategy {

        private final ReactiveTarantoolOperations operations;
        private final MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext;

        public TarantoolQueryLookupStrategy(ReactiveTarantoolOperations operations,
                                            MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext) {
            this.operations = operations;
            this.mappingContext = mappingContext;
        }

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
            ReactiveTarantoolQueryMethod queryMethod = new ReactiveTarantoolQueryMethod(method, metadata, factory, mappingContext);
            if (queryMethod.hasAnnotatedQuery()) {
                return new ReactiveDirectTarantoolQuery(queryMethod, operations);
            } else {
                return new ReactivePartTreeTarantoolQuery(queryMethod, operations);
            }
        }
    }
}
