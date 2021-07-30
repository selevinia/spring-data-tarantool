package org.springframework.data.tarantool.repository.support;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ReactiveExtensionAwareQueryMethodEvaluationContextProvider;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * {@link org.springframework.beans.factory.FactoryBean} to create
 * {@link org.springframework.data.tarantool.repository.ReactiveTarantoolRepository} instances.
 *
 * @author Alexander Rublev
 */
public class ReactiveTarantoolRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends RepositoryFactoryBeanSupport<T, S, ID> {
    private boolean mappingContextConfigured = false;
    private @Nullable ReactiveTarantoolOperations operations;

    /**
     * Creates a new {@link RepositoryFactoryBeanSupport} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    protected ReactiveTarantoolRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /**
     * Configures the {@link ReactiveTarantoolOperations} used for Tarantool data access operations.
     *
     * @param operations {@link ReactiveTarantoolOperations} used to perform CRUD, Query and general data access
     *                   operations on Tarantool.
     */
    public void setReactiveTarantoolOperations(ReactiveTarantoolOperations operations) {
        this.operations = operations;
    }

    @Override
    protected void setMappingContext(MappingContext<?, ?> mappingContext) {
        super.setMappingContext(mappingContext);
        this.mappingContextConfigured = true;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        Assert.state(operations != null, "ReactiveTarantoolOperations must not be null");

        return new ReactiveTarantoolRepositoryFactory(operations);
    }

    @Override
    protected Optional<QueryMethodEvaluationContextProvider> createDefaultQueryMethodEvaluationContextProvider(ListableBeanFactory beanFactory) {
        return Optional.of(new ReactiveExtensionAwareQueryMethodEvaluationContextProvider(beanFactory));
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(operations, "ReactiveTarantoolOperations must not be null!");

        if (!mappingContextConfigured) {
            setMappingContext(operations.getConverter().getMappingContext());
        }
    }
}
