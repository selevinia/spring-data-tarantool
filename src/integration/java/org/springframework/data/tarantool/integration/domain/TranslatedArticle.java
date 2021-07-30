package org.springframework.data.tarantool.integration.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.data.tarantool.core.mapping.PrimaryKey;
import org.springframework.data.tarantool.core.mapping.Space;

@Data
@Builder
@EqualsAndHashCode(of = "id")
@Space("translated_articles")
public class TranslatedArticle {
    @PrimaryKey
    private TranslatedArticleKey id;

    @Field("article_name")
    private String name;
    @Field("article_text")
    private String text;
}
