package org.springframework.data.tarantool.integration.core.util;

import org.springframework.data.tarantool.core.mapping.event.*;
import org.springframework.data.tarantool.integration.domain.Article;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CaptureEventListener extends AbstractTarantoolEventListener<Article> {
    private final List<TarantoolMappingEvent<?>> events = new CopyOnWriteArrayList<>();

    @Override
    public void onBeforeSave(BeforeSaveEvent<Article> event) {
        events.add(event);
    }

    @Override
    public void onAfterSave(AfterSaveEvent<Article> event) {
        events.add(event);
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Article> event) {
        events.add(event);
    }

    @Override
    public void onAfterLoad(AfterLoadEvent<Article> event) {
        events.add(event);
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<Article> event) {
        events.add(event);
    }

    public void clear() {
        events.clear();
    }

    public List<BeforeSaveEvent<Article>> getBeforeSave() {
        return filter(BeforeSaveEvent.class);
    }

    public List<AfterSaveEvent<Article>> getAfterSave() {
        return filter(AfterSaveEvent.class);
    }

    public List<AfterDeleteEvent<Article>> getAfterDelete() {
        return filter(AfterDeleteEvent.class);
    }

    public List<AfterConvertEvent<Article>> getAfterConvert() {
        return filter(AfterConvertEvent.class);
    }

    public List<AfterLoadEvent<Article>> getAfterLoad() {
        return filter(AfterLoadEvent.class);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> filter(Class<? super T> targetType) {
        return (List<T>) events.stream().filter(targetType::isInstance).map(targetType::cast).collect(Collectors.toList());
    }

}
