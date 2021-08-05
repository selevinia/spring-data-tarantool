package org.springframework.data.tarantool.integration.repository.util;

import org.springframework.data.tarantool.core.mapping.event.AbstractTarantoolEventListener;
import org.springframework.data.tarantool.core.mapping.event.AfterSaveEvent;
import org.springframework.data.tarantool.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.tarantool.core.mapping.event.TarantoolMappingEvent;
import org.springframework.data.tarantool.integration.domain.User;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CaptureEventListener extends AbstractTarantoolEventListener<User> {
    private final List<TarantoolMappingEvent<?>> events = new CopyOnWriteArrayList<>();

    @Override
    public void onBeforeSave(BeforeSaveEvent<User> event) {
        events.add(event);
    }

    @Override
    public void onAfterSave(AfterSaveEvent<User> event) {
        events.add(event);
    }

    public void clear() {
        events.clear();
    }

    public List<BeforeSaveEvent<User>> getBeforeSave() {
        return filter(BeforeSaveEvent.class);
    }

    public List<AfterSaveEvent<User>> getAfterSave() {
        return filter(AfterSaveEvent.class);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> filter(Class<? super T> targetType) {
        return (List<T>) events.stream().filter(targetType::isInstance).map(targetType::cast).collect(Collectors.toList());
    }
}
