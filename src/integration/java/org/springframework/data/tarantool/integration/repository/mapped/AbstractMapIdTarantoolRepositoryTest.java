package org.springframework.data.tarantool.integration.repository.mapped;

import io.tarantool.driver.api.conditions.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.MapIdFactory;
import org.springframework.data.tarantool.integration.domain.TranslatedArticleWithMapId;
import org.springframework.data.tarantool.repository.MapIdTarantoolRepository;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertWith;

/**
 * Class contains test methods for Tarantool repository usage with MapId
 */
public abstract class AbstractMapIdTarantoolRepositoryTest {

    @Autowired
    protected TranslatedArticleMapIdRepository translatedArticleRepository;

    @Autowired
    protected TarantoolOperations operations;

    @BeforeEach
    void cleanup() {
        operations.delete(Conditions.any(), TranslatedArticleWithMapId.class);
    }

    @Test
    void shouldNotFindNotExisted() {
        MapId id = MapIdFactory.id(MapId.class)
                .with("articleId", UUID.randomUUID())
                .with("locale", Locale.ENGLISH);
        Optional<TranslatedArticleWithMapId> notFound = translatedArticleRepository.findById(id);
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldDoSaveAndFindById() {
        TranslatedArticleWithMapId translatedArticle = newTranslatedArticle();
        TranslatedArticleWithMapId saved = translatedArticleRepository.save(translatedArticle);
        assertThat(saved).isEqualTo(translatedArticle);

        Optional<TranslatedArticleWithMapId> found = translatedArticleRepository.findById(translatedArticle.getMapId());
        assertWith(found.orElse(null), actual -> {
            assertThat(actual.getArticleId()).isEqualTo(translatedArticle.getArticleId());
            assertThat(actual.getLocale()).isEqualTo(translatedArticle.getLocale());
            assertThat(actual.getName()).isEqualTo(translatedArticle.getName());
            assertThat(actual.getText()).isEqualTo(translatedArticle.getText());
        });
    }

    @Test
    void shouldFindAll() {
        for (int i = 0; i < 4; i++) {
            TranslatedArticleWithMapId translatedArticle = newTranslatedArticle();
            TranslatedArticleWithMapId saved = translatedArticleRepository.save(translatedArticle);
            assertThat(saved).isEqualTo(translatedArticle);
        }

        Iterable<TranslatedArticleWithMapId> found = translatedArticleRepository.findAll();
        assertThat(found).hasSize(4);
    }

    protected TranslatedArticleWithMapId newTranslatedArticle() {
        return TranslatedArticleWithMapId.builder()
                .articleId(UUID.randomUUID())
                .locale(Locale.ENGLISH)
                .name("Selevinia eats tarantool")
                .build();
    }

    protected interface TranslatedArticleMapIdRepository extends MapIdTarantoolRepository<TranslatedArticleWithMapId> {
    }
}
