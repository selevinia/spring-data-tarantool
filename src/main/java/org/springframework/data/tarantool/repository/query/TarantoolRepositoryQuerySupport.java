package org.springframework.data.tarantool.repository.query;

import org.springframework.data.convert.CustomConversions;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * Base class for Tarantool {@link RepositoryQuery} implementations providing common infrastructure
 *
 * @author Alexander Rublev
 */
public abstract class TarantoolRepositoryQuerySupport implements RepositoryQuery {
    private final TarantoolQueryMethod queryMethod;
    private final ReactiveTarantoolOperations operations;

    /**
     * Create a new {@link TarantoolRepositoryQuerySupport} from the given {@link TarantoolQueryMethod}
     * and {@link ReactiveTarantoolOperations}
     * @param queryMethod must not be {@literal null}
     * @param operations must not be {@literal null}
     */
    public TarantoolRepositoryQuerySupport(TarantoolQueryMethod queryMethod, ReactiveTarantoolOperations operations) {
        this.queryMethod = queryMethod;
        this.operations = operations;
    }

    @Override
    public TarantoolQueryMethod getQueryMethod() {
        return this.queryMethod;
    }

    /**
     * Returns the related {@link ReactiveTarantoolOperations}.
     *
     * @return never {@literal null}.
     */
    public ReactiveTarantoolOperations getOperations() {
        return this.operations;
    }

    protected Class<?> resolveResultType(ResultProcessor resultProcessor) {
        TarantoolReturnedType returnedType = new TarantoolReturnedType(resultProcessor.getReturnedType(),
                operations.getConverter().getCustomConversions());

        return (returnedType.isProjecting() ? returnedType.getDomainType() : returnedType.getReturnedType());
    }

    static class TarantoolReturnedType {
        private final ReturnedType returnedType;
        private final CustomConversions customConversions;

        TarantoolReturnedType(ReturnedType returnedType, CustomConversions customConversions) {
            this.returnedType = returnedType;
            this.customConversions = customConversions;
        }

        boolean isProjecting() {
            if (!this.returnedType.isProjecting()) {
                return false;
            }

            if (ClassUtils.isAssignable(Map.class, this.returnedType.getReturnedType())) {
                return false;
            }

            // Type conversion using registered conversions is handled on template level
            if (this.customConversions.hasCustomWriteTarget(this.returnedType.getReturnedType())) {
                return false;
            }

            // Don't apply projection on Tarantool simple types
            return !this.customConversions.isSimpleType(this.returnedType.getReturnedType());
        }

        Class<?> getDomainType() {
            return this.returnedType.getDomainType();
        }

        Class<?> getReturnedType() {
            return this.returnedType.getReturnedType();
        }
    }

}
