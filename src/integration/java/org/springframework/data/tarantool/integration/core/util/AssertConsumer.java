package org.springframework.data.tarantool.integration.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.tarantool.integration.domain.Article;
import org.springframework.data.tarantool.integration.domain.TranslatedArticle;
import org.springframework.data.tarantool.integration.domain.TranslatedArticleWithFlatKey;
import org.springframework.data.tarantool.integration.domain.TranslatedArticleWithMapId;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass
public class AssertConsumer {

    public static Consumer<Article> articleAssertConsumer(Article expected) {
        return (Article actual) -> {
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getSlug()).isEqualTo(expected.getSlug());
            assertThat(actual.getPublishDate().withNano(0)).isEqualTo(expected.getPublishDate().withNano(0));
            assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
            assertThat(actual.getTags()).isEqualTo(expected.getTags());
            assertThat(actual.getLikes()).isEqualTo(expected.getLikes());
        };
    }

    public static Consumer<TranslatedArticle> articleAssertConsumer(TranslatedArticle expected) {
        return (TranslatedArticle actual) -> {
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getText()).isEqualTo(expected.getText());
        };
    }

    public static Consumer<TranslatedArticleWithFlatKey> articleAssertConsumer(TranslatedArticleWithFlatKey expected) {
        return (TranslatedArticleWithFlatKey actual) -> {
            assertThat(actual.getArticleId()).isEqualTo(expected.getArticleId());
            assertThat(actual.getLocale()).isEqualTo(expected.getLocale());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getText()).isEqualTo(expected.getText());
        };
    }

    public static Consumer<TranslatedArticleWithMapId> articleAssertConsumer(TranslatedArticleWithMapId expected) {
        return (TranslatedArticleWithMapId actual) -> {
            assertThat(actual.getArticleId()).isEqualTo(expected.getArticleId());
            assertThat(actual.getLocale()).isEqualTo(expected.getLocale());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getText()).isEqualTo(expected.getText());
        };
    }

}
