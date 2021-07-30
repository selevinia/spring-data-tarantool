package org.springframework.data.tarantool.repository.query;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.util.ReactiveWrappers;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.tarantool.repository.Query;
import org.springframework.data.tarantool.repository.support.MappingTarantoolEntityInformation;
import org.springframework.data.tarantool.repository.support.TarantoolEntityMetadata;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reactive specific implementation of {@link QueryMethod}.
 *
 * @author Alexander Rublev
 */
public class TarantoolQueryMethod extends QueryMethod {
    private final Lazy<Boolean> isCollectionQuery;
    private final Optional<Query> query;
    private final MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext;
    private @Nullable
    TarantoolEntityMetadata<?> entityMetadata;

    /**
     * Creates a new {@link QueryMethod} from the given parameters. Looks up the correct query to use for following
     * invocations of the method given.
     *
     * @param method   must not be {@literal null}.
     * @param metadata must not be {@literal null}.
     * @param factory  must not be {@literal null}.
     */
    public TarantoolQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                MappingContext<? extends TarantoolPersistentEntity<?>, ? extends TarantoolPersistentProperty> mappingContext) {
        super(method, metadata, factory);
        this.query = Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(method, Query.class));
        this.mappingContext = mappingContext;
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

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public TarantoolEntityMetadata<?> getEntityInformation() {
        if (entityMetadata == null) {
            Class<?> domainClass = getDomainClass();
            entityMetadata = new MappingTarantoolEntityInformation(mappingContext.getRequiredPersistentEntity(domainClass));
        }

        return entityMetadata;
    }

    /**
     * Returns whether the method has an annotated query function.
     */
    public boolean hasAnnotatedQuery() {
        return this.query.map(Query::function).filter(StringUtils::hasText).isPresent();
    }

    /**
     * Returns the query function name declared in a {@link Query} annotation or {@literal null} if neither the annotation found
     * nor the attribute was specified.
     *
     * @return the function name or {@literal null} if no query query function present.
     */
    @Nullable
    public String getAnnotatedQuery() {
        return query.map(Query::function).orElse(null);
    }
}
