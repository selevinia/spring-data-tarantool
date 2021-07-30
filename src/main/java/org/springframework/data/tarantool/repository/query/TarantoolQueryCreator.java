package org.springframework.data.tarantool.repository.query;

import io.tarantool.driver.api.conditions.Conditions;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.tarantool.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.Optional;

/**
 * Custom query creator to create Tarantool criteria
 *
 * @author Alexander Rublev
 */
public class TarantoolQueryCreator extends AbstractQueryCreator<Query, Conditions> {
    private final MappingContext<?, TarantoolPersistentProperty> mappingContext;
    private final TarantoolConverter converter;
    private final boolean proxyClient;
    private final Query query;

    /**
     * Create a new {@link TarantoolQueryCreator} from the given {@link PartTree}, {@link ParametersParameterAccessor} and
     * {@link MappingContext}.
     *
     * @param tree              must not be {@literal null}.
     * @param parameterAccessor must not be {@literal null}.
     * @param converter         must not be {@literal null}.
     * @param proxyClient       do we use ProxyTarantoolClient or not
     */
    public TarantoolQueryCreator(PartTree tree, ParametersParameterAccessor parameterAccessor,
                                 TarantoolConverter converter, boolean proxyClient) {
        super(tree, parameterAccessor);

        Assert.notNull(converter, "TarantoolConverter must not be null");
        this.converter = converter;
        this.proxyClient = proxyClient;
        this.mappingContext = converter.getMappingContext();

        if (tree.isLimiting()) {
            this.query = new Query(tree.getMaxResults() == null ? 0 : tree.getMaxResults());
        } else {
            this.query = new Query();
        }
    }

    @Override
    protected Conditions create(Part part, Iterator<Object> iterator) {
        PersistentPropertyPath<TarantoolPersistentProperty> path = mappingContext.getPersistentPropertyPath(part.getProperty());
        TarantoolPersistentProperty property = path.getLeafProperty();

        Assert.state(property != null && path.toDotPath() != null, "Leaf property must not be null");

        return from(part, property, iterator);
    }

    @Override
    protected Conditions and(Part part, Conditions base, Iterator<Object> iterator) {
        return create(part, iterator);
    }

    @Override
    protected Conditions or(Conditions base, Conditions criteria) {
        throw new InvalidDataAccessApiUsageException("Tarantool does not support an OR operator");
    }

    @Override
    protected Query complete(@Nullable Conditions criteria, Sort sort) {
        return query;
    }

    private Conditions from(Part part, TarantoolPersistentProperty property, Iterator<Object> parameters) {
        Part.Type type = part.getType();

        switch (type) {
            case AFTER:
            case GREATER_THAN:
                return query.getConditions().andGreaterThan(property.getFieldName(), getValue(property, parameters));
            case GREATER_THAN_EQUAL:
                return query.getConditions().andGreaterOrEquals(property.getFieldName(), getValue(property, parameters));
            case BEFORE:
            case LESS_THAN:
                return query.getConditions().andLessThan(property.getFieldName(), getValue(property, parameters));
            case LESS_THAN_EQUAL:
                return query.getConditions().andLessOrEquals(property.getFieldName(), getValue(property, parameters));
            case BETWEEN:
                if (!proxyClient) {
                    throw new UnsupportedOperationException("Multiple conditions on same index not supported yet in driver");
                }

                Object value = getValue(property, parameters);
                if (!(value instanceof Range)) {
                    return query.getConditions().andGreaterOrEquals(property.getFieldName(), value)
                            .andLessOrEquals(property.getFieldName(), getValue(property, parameters));
                }

                Range<?> range = (Range<?>) value;
                Optional<?> min = range.getLowerBound().getValue();
                Optional<?> max = range.getUpperBound().getValue();

                min.ifPresent(it -> {
                    if (range.getLowerBound().isInclusive()) {
                        query.getConditions().andGreaterOrEquals(property.getFieldName(), it);
                    } else {
                        query.getConditions().andGreaterThan(property.getFieldName(), it);
                    }
                });

                max.ifPresent(it -> {
                    if (range.getUpperBound().isInclusive()) {
                        query.getConditions().andLessOrEquals(property.getFieldName(), it);
                    } else {
                        query.getConditions().andLessThan(property.getFieldName(), it);
                    }
                });

                return query.getConditions();
            case TRUE:
                return query.getConditions().andEquals(property.getFieldName(), true);
            case FALSE:
                return query.getConditions().andEquals(property.getFieldName(), false);
            case SIMPLE_PROPERTY:
                return query.getConditions().andEquals(property.getFieldName(), getValue(property, parameters));
            default:
                throw new InvalidDataAccessApiUsageException(
                        String.format("Unsupported keyword [%s] in part [%s]", type, part));
        }
    }

    private Object getValue(TarantoolPersistentProperty property, Iterator<Object> parameters) {
        Object value = parameters.next();
        if (!value.getClass().equals(property.getType())) {
            throw new InvalidDataAccessApiUsageException(
                    String.format("Unsupported parameter type usage, should be %s but was %s", property.getType(), value.getClass()));
        }

        return converter.convertToWritableType(value);
    }
}
