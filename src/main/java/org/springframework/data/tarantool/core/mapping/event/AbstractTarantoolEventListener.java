package org.springframework.data.tarantool.core.mapping.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;

/**
 * Base class to implement domain specific {@link ApplicationListener}s for {@link TarantoolMappingEvent}.
 *
 * @author Alexander Rublev
 */
public class AbstractTarantoolEventListener<E> implements ApplicationListener<TarantoolMappingEvent<?>> {
    protected static final Logger log = LoggerFactory.getLogger(AbstractTarantoolEventListener.class);
    private final Class<?> domainClass;

    /**
     * Creates a new {@link AbstractTarantoolEventListener}.
     */
    public AbstractTarantoolEventListener() {
        Class<?> typeArgument = GenericTypeResolver.resolveTypeArgument(getClass(), AbstractTarantoolEventListener.class);
        this.domainClass = typeArgument == null ? Object.class : typeArgument;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void onApplicationEvent(TarantoolMappingEvent<?> event) {
        Object source = event.getSource();

        if (event instanceof AfterLoadEvent) {
            AfterLoadEvent<?> afterLoadEvent = (AfterLoadEvent<?>) event;
            if (domainClass.isAssignableFrom(afterLoadEvent.getType())) {
                onAfterLoad((AfterLoadEvent<E>) event);
            }
            return;
        }

        if (event instanceof AfterDeleteEvent) {
            AfterDeleteEvent<?> afterDeleteEvent = (AfterDeleteEvent<?>) event;
            if (domainClass.isAssignableFrom(afterDeleteEvent.getType())) {
                onAfterDelete((AfterDeleteEvent<E>) event);
            }
            return;
        }

        // Check for matching domain type and invoke callbacks.
        if (!domainClass.isAssignableFrom(source.getClass())) {
            return;
        }

        if (event instanceof BeforeSaveEvent) {
            onBeforeSave((BeforeSaveEvent<E>) event);
        } else if (event instanceof AfterSaveEvent) {
            onAfterSave((AfterSaveEvent<E>) event);
        } else if (event instanceof AfterConvertEvent) {
            onAfterConvert((AfterConvertEvent<E>) event);
        }
    }

    /**
     * Captures {@link BeforeSaveEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public void onBeforeSave(BeforeSaveEvent<E> event) {
        if (log.isDebugEnabled()) {
            log.debug("onBeforeSave({})", event.getSource());
        }
    }

    /**
     * Captures {@link AfterSaveEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public void onAfterSave(AfterSaveEvent<E> event) {
        if (log.isDebugEnabled()) {
            log.debug("onAfterSave({})", event.getSource());
        }
    }

    /**
     * Captures {@link AfterDeleteEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public void onAfterDelete(AfterDeleteEvent<E> event) {
        if (log.isDebugEnabled()) {
            log.debug("onAfterDelete({})", event.getSource());
        }
    }

    /**
     * Captures {@link AfterLoadEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public void onAfterLoad(AfterLoadEvent<E> event) {
        if (log.isDebugEnabled()) {
            log.debug("onAfterLoad({})", event.getSource());
        }
    }

    /**
     * Captures {@link AfterConvertEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public void onAfterConvert(AfterConvertEvent<E> event) {
        if (log.isDebugEnabled()) {
            log.debug("onAfterConvert({})", event.getSource());
        }
    }

}