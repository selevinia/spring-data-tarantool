package org.springframework.data.tarantool.repository.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.repository.TarantoolRepository;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link FactoryBean} to create {@link TarantoolRepository} instances.
 *
 * @author AlexAnder Rublev
 */
public class TarantoolRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends RepositoryFactoryBeanSupport<T, S, ID> {
    private boolean mappingContextConfigured = false;
    private @Nullable TarantoolOperations operations;

    /**
     * Create a new {@link TarantoolRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public TarantoolRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /**
     * Configures the {@link TarantoolOperations} used for Tarantool data access operations.
     *
     * @param operations {@link TarantoolOperations} used to perform CRUD, Query and general data access
     *                   operations on Tarantool.
     */
    public void setTarantoolOperations(TarantoolOperations operations) {
        this.operations = operations;
    }

    @Override
    protected void setMappingContext(MappingContext<?, ?> mappingContext) {
        super.setMappingContext(mappingContext);
        this.mappingContextConfigured = true;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        Assert.state(operations != null, "TarantoolOperations must not be null");

        return new TarantoolRepositoryFactory(operations);
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
