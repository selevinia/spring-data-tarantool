package org.springframework.data.tarantool.integration.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.tarantool.core.mapping.PrimaryKeyClass;
import org.springframework.data.tarantool.core.mapping.PrimaryKeyField;

import java.util.Locale;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode
@PrimaryKeyClass
public class TranslatedArticleKey {
    @PrimaryKeyField("article_id")
    private UUID articleId;
    @PrimaryKeyField
    private Locale locale;
}
