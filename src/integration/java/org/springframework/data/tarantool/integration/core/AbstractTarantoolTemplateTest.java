package org.springframework.data.tarantool.integration.core;

import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.protocol.TarantoolIndexQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.tarantool.TarantoolDataRetrievalException;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.core.TarantoolTemplate;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.MapIdFactory;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.integration.domain.*;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.tarantool.integration.core.util.AssertConsumer.articleAssertConsumer;
import static org.springframework.data.tarantool.integration.core.util.TestConfigProvider.*;
import static org.springframework.data.tarantool.integration.core.util.TestData.*;

/**
 * Class contains all test methods for Tarantool template usage
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTarantoolTemplateTest {

    private TarantoolTemplate tarantoolTemplate;

    public abstract TarantoolClientOptions getOptions();

    @BeforeAll
    void setUp() {
        TarantoolMappingContext mappingContext = mappingContext();
        tarantoolTemplate = new TarantoolTemplate(clientFactory(getOptions()).createClient(), converter(mappingContext), exceptionTranslator());
    }

    @BeforeEach
    void clear() {
        tarantoolTemplate.delete(Conditions.any(), Article.class);
        tarantoolTemplate.delete(Conditions.any(), User.class);
        tarantoolTemplate.delete(Conditions.any(), Comment.class);
        tarantoolTemplate.delete(Conditions.any(), TranslatedArticle.class);
    }

    @Test
    void shouldGetVersion() {
        TarantoolVersion version = tarantoolTemplate.getVersion();
        assertThat(version).isNotNull();
        assertThat(version.toString()).contains("Tarantool 2.9.0");
    }

    @Test
    void shouldInsertArticle() {
        Article article = article();

        Article selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(article, Article.class);
        assertThat(selected).isEqualTo(article);

        selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isEqualTo(article);
    }

    @Test
    void shouldNotInsertEmptyArticle() {
        Article article = emptyArticle();

        assertThatThrownBy(() -> tarantoolTemplate.insert(article, Article.class))
                .isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageEndingWith("required by space format is missing");
    }

    @Test
    void shouldInsertTranslatedArticle() {
        TranslatedArticle translatedArticle = translatedArticle();
        TranslatedArticleKey translatedArticleId = translatedArticle.getId();

        TranslatedArticle selected = tarantoolTemplate.selectById(translatedArticleId, TranslatedArticle.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(translatedArticle, TranslatedArticle.class);
        assertThat(selected).isEqualTo(translatedArticle);

        selected = tarantoolTemplate.selectById(translatedArticleId, TranslatedArticle.class);
        assertWith(selected, articleAssertConsumer(translatedArticle));
    }

    @Test
    void shouldInsertTranslatedArticleWithFlatKey() {
        TranslatedArticleWithFlatKey translatedArticle = translatedArticleWithFlatKey();
        MapId translatedArticleMapId = MapIdFactory.id(MapId.class)
                .with("articleId", translatedArticle.getArticleId())
                .with("locale", translatedArticle.getLocale());

        TranslatedArticleWithFlatKey selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithFlatKey.class);
        assertThat(selected).isEqualTo(translatedArticle);

        selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class);
        assertWith(selected, articleAssertConsumer(translatedArticle));
    }

    @Test
    void shouldInsertTranslatedArticleWithMapId() {
        TranslatedArticleWithMapId translatedArticle = translatedArticleWithMapId();
        MapId translatedArticleMapId = translatedArticle.getMapId();

        TranslatedArticleWithMapId selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithMapId.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithMapId.class);
        assertThat(selected).isEqualTo(translatedArticle);

        selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithMapId.class);
        assertWith(selected, articleAssertConsumer(translatedArticle));
    }

    @Test
    void shouldReplaceArticle() {
        Article article = simpleArticle();

        Article inserted = tarantoolTemplate.insert(article, Article.class);
        assertThat(inserted).isEqualTo(article);

        article.setLikes(10);
        inserted = tarantoolTemplate.replace(article, Article.class);
        assertThat(inserted).isEqualTo(article);
    }

    @Test
    void shouldInsertArticleWithReplace() {
        Article article = simpleArticle();

        Article replaced = tarantoolTemplate.replace(article, Article.class);
        assertThat(replaced).isEqualTo(article);

        Article selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isEqualTo(article);
    }

    @Test
    void shouldUpdateArticle() {
        Article article = article();

        Article inserted = tarantoolTemplate.insert(article, Article.class);
        assertThat(inserted).isEqualTo(article);

        Article selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isEqualTo(article);

        Article partialArticle = Article.builder()
                .tags(List.of(new Tag("test")))
                .likes(article.getLikes() + 1)
                .build();
        article.setTags(partialArticle.getTags());
        article.setLikes(partialArticle.getLikes());
        List<Article> updated = tarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(article.getId())), partialArticle, Article.class);
        assertWith(updated.get(0), articleAssertConsumer(article));
    }

}
