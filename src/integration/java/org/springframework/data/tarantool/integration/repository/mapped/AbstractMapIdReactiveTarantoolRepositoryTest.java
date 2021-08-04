package org.springframework.data.tarantool.integration.repository.mapped;

import io.tarantool.driver.api.conditions.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.MapIdFactory;
import org.springframework.data.tarantool.integration.domain.TranslatedArticleWithMapId;
import org.springframework.data.tarantool.repository.MapIdReactiveTarantoolRepository;
import reactor.test.StepVerifier;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class contains test methods for Tarantool reactive repository usage with MapId
 */
public abstract class AbstractMapIdReactiveTarantoolRepositoryTest {

    @Autowired
    protected TranslatedArticleMapIdRepository translatedArticleRepository;

    @Autowired
    protected ReactiveTarantoolOperations operations;

    @BeforeEach
    void cleanup() {
        operations.delete(Conditions.any(), TranslatedArticleWithMapId.class).then().as(StepVerifier::create).verifyComplete();
    }

    @Test
    void shouldNotFindNotExisted() {
        MapId id = MapIdFactory.id(MapId.class)
                .with("articleId", UUID.randomUUID())
                .with("locale", Locale.ENGLISH);
        translatedArticleRepository.findById(id).as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldDoSaveAndFindById() {
        TranslatedArticleWithMapId translatedArticle = newTranslatedArticle();
        translatedArticleRepository.save(translatedArticle).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        translatedArticleRepository.findById(translatedArticle.getMapId()).as(StepVerifier::create)
                .assertNext(actual -> {
                    assertThat(actual.getArticleId()).isEqualTo(translatedArticle.getArticleId());
                    assertThat(actual.getLocale()).isEqualTo(translatedArticle.getLocale());
                    assertThat(actual.getName()).isEqualTo(translatedArticle.getName());
                    assertThat(actual.getText()).isEqualTo(translatedArticle.getText());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAll() {
        for (int i = 0; i < 4; i++) {
            TranslatedArticleWithMapId translatedArticle = newTranslatedArticle();
            translatedArticleRepository.save(translatedArticle).as(StepVerifier::create)
                    .expectNext(translatedArticle)
                    .verifyComplete();
        }

        translatedArticleRepository.findAll().as(StepVerifier::create)
                .expectNextCount(4L)
                .verifyComplete();
    }

    protected TranslatedArticleWithMapId newTranslatedArticle() {
        return TranslatedArticleWithMapId.builder()
                .articleId(UUID.randomUUID())
                .locale(Locale.ENGLISH)
                .name("Selevinia eats tarantool")
                .build();
    }

    protected interface TranslatedArticleMapIdRepository extends MapIdReactiveTarantoolRepository<TranslatedArticleWithMapId> {
    }
}
