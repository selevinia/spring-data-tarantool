package org.springframework.data.tarantool.repository.query;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.util.ReactiveWrappers;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.util.Lazy;

import java.lang.reflect.Method;

/**
 * Reactive specific implementation of {@link QueryMethod}.
 *
 * @author Alexander Rublev
 */
public class ReactiveTarantoolQueryMethod extends TarantoolQueryMethod {
    private final Lazy<Boolean> isCollectionQuery;

    /**
     * Creates a new {@link QueryMethod} from the given parameters. Looks up the correct query to use for following
     * invocations of the method given.
     *
     * @param method         must not be {@literal null}.
     * @param metadata       must not be {@literal null}.
     * @param factory        must not be {@literal null}.
     * @param mappingContext must not be {@literal null}
     */
    public ReactiveTarantoolQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                        MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext) {
        super(method, metadata, factory, mappingContext);
        this.isCollectionQuery = Lazy.of(() -> ReactiveWrappers.isMultiValueType(metadata.getReturnType(method).getType()) || super.isCollectionQuery());
    }

    @Override
    public boolean isCollectionQuery() {
        return isCollectionQuery.get();
    }

    /*
     * All reactive query methods are streaming queries.
     */
    @Override
    public boolean isStreamQuery() {
        return true;
    }

}
