package org.springframework.data.tarantool.core.mapping.event;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.mapping.callback.EntityCallback;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

/**
 * {@link EntityCallback} to populate auditing related fields on an entity about to be saved.
 *
 * @author Alexander Rublev
 */
public class AuditingEntityCallback implements BeforeConvertCallback<Object>, Ordered {

    private final ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory;

    /**
     * Creates a new {@link AuditingEntityCallback} using the given {@link MappingContext} and {@link AuditingHandler}
     * provided by the given {@link ObjectFactory}.
     *
     * @param auditingHandlerFactory must not be {@literal null}.
     */
    public AuditingEntityCallback(ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory) {
        Assert.notNull(auditingHandlerFactory, "IsNewAwareAuditingHandler must not be null!");
        this.auditingHandlerFactory = auditingHandlerFactory;
    }

    @Override
    public Object onBeforeConvert(Object entity, String spaceName) {
        return auditingHandlerFactory.getObject().markAudited(entity);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
