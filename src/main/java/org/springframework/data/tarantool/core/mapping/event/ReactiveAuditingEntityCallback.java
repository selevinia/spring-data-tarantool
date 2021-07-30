package org.springframework.data.tarantool.core.mapping.event;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.ReactiveIsNewAwareAuditingHandler;
import org.springframework.data.mapping.callback.EntityCallback;
import org.springframework.util.Assert;

/**
 * Reactive {@link EntityCallback} to populate auditing related fields on an entity about to be saved.
 *
 * @author Alexander Rublev
 */
public class ReactiveAuditingEntityCallback implements ReactiveBeforeConvertCallback<Object>, Ordered {
    private final ObjectFactory<ReactiveIsNewAwareAuditingHandler> auditingHandlerFactory;

    /**
     * Creates a new {@link ReactiveAuditingEntityCallback} using the given {@link AuditingHandler}
     * provided by the given {@link ObjectFactory}.
     *
     * @param auditingHandlerFactory must not be {@literal null}.
     */
    public ReactiveAuditingEntityCallback(ObjectFactory<ReactiveIsNewAwareAuditingHandler> auditingHandlerFactory) {
        Assert.notNull(auditingHandlerFactory, "IsNewAwareAuditingHandler must not be null!");
        this.auditingHandlerFactory = auditingHandlerFactory;
    }

    @Override
    public Publisher<Object> onBeforeConvert(Object entity, String collection) {
        return auditingHandlerFactory.getObject().markAudited(entity);
    }

    @Override
    public int getOrder() {
        return 100;
    }

}
